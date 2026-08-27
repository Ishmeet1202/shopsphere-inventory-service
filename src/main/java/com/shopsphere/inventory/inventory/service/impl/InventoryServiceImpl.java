package com.shopsphere.inventory.inventory.service.impl;

import com.shopsphere.inventory.exception.DuplicateInventoryException;
import com.shopsphere.inventory.exception.InventoryNotFoundException;
import com.shopsphere.inventory.inventory.dto.request.AddStockRequestDto;
import com.shopsphere.inventory.inventory.dto.request.InventoryCreateRequestDto;
import com.shopsphere.inventory.inventory.dto.response.InventoryResponseDto;
import com.shopsphere.inventory.inventory.entity.Inventory;
import com.shopsphere.inventory.inventory.mapper.InventoryMapper;
import com.shopsphere.inventory.inventory.repository.InventoryRepository;
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

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

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
        LOGGER.info("addStock: updated inventory for productId={}, newQuantity={}, tenantId={}", productId, inventory.getAvailableQuantity(), tenantId);

        return inventoryMapper.toInventoryResponseDto(inventory);
    }


    private void initializeNewInventory(Inventory inventory, String tenantId) {
        inventory.setReservedQuantity(0);
        inventory.setTenantId(tenantId);
    }
}
