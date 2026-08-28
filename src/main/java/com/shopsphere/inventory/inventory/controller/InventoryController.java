package com.shopsphere.inventory.inventory.controller;

import com.shopsphere.inventory.inventory.dto.request.AddStockRequestDto;
import com.shopsphere.inventory.inventory.dto.request.AdjustStockRequestDto;
import com.shopsphere.inventory.inventory.dto.request.InventoryCreateRequestDto;
import com.shopsphere.inventory.inventory.dto.response.InventoryResponseDto;
import com.shopsphere.inventory.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponseDto> createInventory(
            @Valid @RequestBody InventoryCreateRequestDto request
    ) {
        InventoryResponseDto response = inventoryService.createInventory(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponseDto> getInventoryByProductId(
            @PathVariable(name = "productId") String productId
    ) {
        InventoryResponseDto response = inventoryService.getInventoryByProductId(productId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/{productId}/stock")
    public ResponseEntity<InventoryResponseDto> addStock(
            @PathVariable(name = "productId") String productId,
            @Valid @RequestBody AddStockRequestDto request
    ) {
        InventoryResponseDto response = inventoryService.addStock(productId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/{productId}/adjustment")
    public ResponseEntity<InventoryResponseDto> adjustStock(
            @PathVariable(name = "productId") String productId,
            @Valid @RequestBody AdjustStockRequestDto request
    ) {
        InventoryResponseDto response = inventoryService.adjustStock(productId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }
}
