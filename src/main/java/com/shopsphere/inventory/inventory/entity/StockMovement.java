package com.shopsphere.inventory.inventory.entity;

import com.shopsphere.inventory.inventory.enums.StockMovementType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private StockMovementType movementType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 500)
    private String reason;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id")
    private String referenceId;
}
