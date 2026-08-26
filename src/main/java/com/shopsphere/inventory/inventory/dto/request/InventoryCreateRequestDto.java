package com.shopsphere.inventory.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCreateRequestDto {
    @NotBlank(message = "Product ID cannot be blank.")
    private String productId;

    @NotNull(message = "Product quantity cannot be null.")
    @Positive(message = "Product quantity must be greater than zero.")
    private Integer quantity;
}
