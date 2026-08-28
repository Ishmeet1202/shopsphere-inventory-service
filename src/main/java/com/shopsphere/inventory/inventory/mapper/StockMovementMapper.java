package com.shopsphere.inventory.inventory.mapper;

import com.shopsphere.inventory.inventory.entity.StockMovement;
import com.shopsphere.inventory.inventory.enums.StockMovementType;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {

    public StockMovement toStockMovementEntity(
            String tenantId,
            String productId,
            StockMovementType movementType,
            Integer quantity,
            String reason,
            String referenceType,
            String referenceId
    ) {
        return StockMovement.builder()
                .productId(productId)
                .tenantId(tenantId)
                .quantity(quantity)
                .movementType(movementType)
                .reason(reason)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build();

    }
}
