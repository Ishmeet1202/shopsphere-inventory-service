package com.shopsphere.inventory.inventory.dto.request;

import com.shopsphere.inventory.inventory.enums.StockAdjustmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustStockRequestDto {
    @NotBlank(message = "Adjustment type cannot be blank.")
    @Length(max = 50, message = "Adjustment type must not exceed 50 characters.")
    private StockAdjustmentType type;

    @NotNull(message = "Adjustment quantity cannot be blank.")
    @Positive(message = "Adjustment quantity must be a positive integer.")
    private Integer quantity;

    @NotNull(message = "Adjustment reason cannot be blank.")
    @Length(max = 200, message = "Adjustment reason must not exceed 200 characters.")
    private String reason;
}
