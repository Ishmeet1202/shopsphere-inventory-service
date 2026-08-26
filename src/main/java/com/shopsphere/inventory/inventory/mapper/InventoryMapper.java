package com.shopsphere.inventory.inventory.mapper;

import com.shopsphere.inventory.inventory.dto.request.InventoryCreateRequestDto;
import com.shopsphere.inventory.inventory.dto.response.InventoryResponseDto;
import com.shopsphere.inventory.inventory.entity.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public Inventory toInventoryEntity(InventoryCreateRequestDto request) {
        return Inventory.builder()
                .productId(request.getProductId())
                .availableQuantity(request.getQuantity())
                .build();
    }

    public InventoryResponseDto toInventoryResponseDto(Inventory inventory) {
        return InventoryResponseDto.builder()
                .productId(inventory.getProductId())
                .availableQuantity(inventory.getAvailableQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .build();
    }
}
