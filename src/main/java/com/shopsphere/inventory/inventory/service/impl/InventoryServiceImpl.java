package com.shopsphere.inventory.inventory.service.impl;

import com.shopsphere.inventory.exception.DuplicateInventoryException;
import com.shopsphere.inventory.exception.InvalidStockAdjustmentException;
import com.shopsphere.inventory.exception.InventoryNotFoundException;
import com.shopsphere.inventory.inventory.dto.request.AddStockRequestDto;
import com.shopsphere.inventory.inventory.dto.request.AdjustStockRequestDto;
import com.shopsphere.inventory.inventory.dto.request.InventoryCreateRequestDto;
import com.shopsphere.inventory.inventory.dto.response.InventoryResponseDto;
import com.shopsphere.inventory.inventory.entity.Inventory;
import com.shopsphere.inventory.inventory.entity.StockMovement;
import com.shopsphere.inventory.inventory.enums.StockAdjustmentType;
import com.shopsphere.inventory.inventory.enums.StockMovementType;
import com.shopsphere.inventory.inventory.mapper.InventoryMapper;
import com.shopsphere.inventory.inventory.mapper.StockMovementMapper;
import com.shopsphere.inventory.inventory.repository.InventoryRepository;
import com.shopsphere.inventory.inventory.repository.StockMovementRepository;
import com.shopsphere.inventory.inventory.service.InventoryService;
import com.shopsphere.inventory.tenant.context.TenantContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);
    private static final String INVENTORY_ALREADY_EXISTS = "Inventory already exists for the given product and tenant";
    private static final String INVENTORY_NOT_FOUND = "Inventory not found for productId: ";
    private static final String STOCK_ADDED_REASON = "Stock added";
    private static final String STOCK_ADDED_REASON_NEW_PRODUCT = "Initial stock";
    private static final String INVALID_STOCK_ADJUSTMENT = "Invalid stock adjustment: resulting quantity cannot be negative";

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final StockMovementMapper stockMovementMapper;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional
    public InventoryResponseDto createInventory(InventoryCreateRequestDto request) {
        String tenantId = TenantContext.requireTenantId();
        LOGGER.info("createInventory: productId={}, quantity={}, tenantId={}", request.getProductId(), request.getQuantity(), tenantId);

        boolean exists = inventoryRepository.existsByProductIdAndTenantId(request.getProductId(), tenantId);

        if (exists) {
            LOGGER.error("createInventory: inventory already exists for productId={}, tenantId={}", request.getProductId(), tenantId);
            throw new DuplicateInventoryException(INVENTORY_ALREADY_EXISTS);
        }

        Inventory inventory = inventoryMapper.toInventoryEntity(request);

        initializeNewInventory(inventory, tenantId);

        inventory = inventoryRepository.save(inventory);

        StockMovement stockMovement = stockMovementMapper.toStockMovementEntity(
                tenantId,
                inventory.getProductId(),
                StockMovementType.STOCK_IN,
                request.getQuantity(),
                STOCK_ADDED_REASON_NEW_PRODUCT,
                null,
                null
        );
        stockMovementRepository.save(stockMovement);
        LOGGER.info("createInventory: saved inventory for productId={}, tenantId={}", inventory.getProductId(), tenantId);
        return inventoryMapper.toInventoryResponseDto(inventory);
    }

    @Override
    public InventoryResponseDto getInventoryByProductId(String productId) {
        String tenantId = TenantContext.requireTenantId();
        LOGGER.info("getInventoryByProductId: productId={}, tenantId={}", productId, tenantId);

        Inventory inventory = inventoryRepository.findByProductIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> {
                    LOGGER.error("getInventoryByProductId: inventory not found for productId={}, tenantId={}", productId, tenantId);
                    return new InventoryNotFoundException(INVENTORY_NOT_FOUND + productId);
                });

        LOGGER.info("getInventoryByProductId: found inventory for productId={}, tenantId={}", productId, tenantId);
        return inventoryMapper.toInventoryResponseDto(inventory);
    }

    @Override
    @Transactional
    public InventoryResponseDto addStock(String productId, AddStockRequestDto request) {
        String tenantId = TenantContext.requireTenantId();
        LOGGER.info("addStock: productId={}, additionalQuantity={}, tenantId={}", productId, request.getQuantity(), tenantId);

        Inventory inventory = inventoryRepository.findByProductIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> {
                    LOGGER.error("addStock: inventory not found for productId={}, tenantId={}", productId, tenantId);
                    return new InventoryNotFoundException(INVENTORY_NOT_FOUND + productId);
                });

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + request.getQuantity());
        inventory = inventoryRepository.save(inventory);

        StockMovement stockMovement = stockMovementMapper.toStockMovementEntity(
                tenantId,
                productId,
                StockMovementType.STOCK_IN,
                request.getQuantity(),
                STOCK_ADDED_REASON,
                null,
                null
        );
        stockMovementRepository.save(stockMovement);
        LOGGER.info("addStock: updated inventory for productId={}, newQuantity={}, tenantId={}", productId, inventory.getAvailableQuantity(), tenantId);

        return inventoryMapper.toInventoryResponseDto(inventory);
    }

    @Override
    @Transactional
    public InventoryResponseDto adjustStock(String productId, AdjustStockRequestDto request) {
        String tenantId = TenantContext.requireTenantId();
        LOGGER.info("adjustStock: productId={}, adjustedQuantity={}, tenantId={}", productId, request.getQuantity(), tenantId);

        Inventory inventory = inventoryRepository.findByProductIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> {
                    LOGGER.error("adjustStock: inventory not found for productId={}, tenantId={}", productId, tenantId);
                    return new InventoryNotFoundException(INVENTORY_NOT_FOUND + productId);
                });

        Integer oldQuantity = inventory.getAvailableQuantity();
        Integer newQuantity = resolveAdjustmentQuantity(
                request.getType(),
                oldQuantity,
                request.getQuantity()
        );

        if (newQuantity < 0) {
            LOGGER.error("adjustStock: invalid stock adjustment for productId={}, oldQuantity={}, adjustedQuantity={}, newQuantity={}, tenantId={}",
                    productId, oldQuantity, request.getQuantity(), newQuantity, tenantId);
            throw new InvalidStockAdjustmentException(INVALID_STOCK_ADJUSTMENT);
        }

        inventory.setAvailableQuantity(newQuantity);
        inventory = inventoryRepository.save(inventory);

        Integer movementQuantity = StockAdjustmentType.DECREASE == request.getType()
                ? -request.getQuantity()
                : request.getQuantity();

        StockMovement stockMovement = stockMovementMapper.toStockMovementEntity(
                tenantId,
                productId,
                StockMovementType.ADJUSTMENT,
                movementQuantity,
                request.getReason(),
                null,
                null
        );
        stockMovementRepository.save(stockMovement);
        LOGGER.info("adjustStock: updated inventory for productId={}, oldQuantity={}, newQuantity={}, tenantId={}", productId, oldQuantity, newQuantity, tenantId);

        return inventoryMapper.toInventoryResponseDto(inventory);
    }


    private void initializeNewInventory(Inventory inventory, String tenantId) {
        inventory.setReservedQuantity(0);
        inventory.setTenantId(tenantId);
    }

    private Integer resolveAdjustmentQuantity(StockAdjustmentType type, Integer oldQuantity, Integer adjustedQuantity) {
        return switch (type) {
            case INCREASE -> oldQuantity + adjustedQuantity;
            case DECREASE -> oldQuantity - adjustedQuantity;
        };
    }
}
