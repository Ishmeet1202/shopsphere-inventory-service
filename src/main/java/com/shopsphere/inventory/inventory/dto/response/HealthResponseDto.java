package com.shopsphere.inventory.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponseDto {
    private String serviceName;
    private String status;
    private String version;
}
