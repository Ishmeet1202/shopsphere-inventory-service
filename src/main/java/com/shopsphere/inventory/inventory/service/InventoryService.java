package com.shopsphere.inventory.inventory.service;

import com.shopsphere.inventory.inventory.dto.request.InventoryCreateRequestDto;
import com.shopsphere.inventory.inventory.dto.response.InventoryResponseDto;

public interface InventoryService {
    InventoryResponseDto createInventory(InventoryCreateRequestDto request);
}
