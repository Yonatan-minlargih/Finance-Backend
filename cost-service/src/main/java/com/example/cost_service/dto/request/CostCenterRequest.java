package com.example.cost_service.dto.request;

import com.example.cost_service.enums.ActiveStatus;
import com.example.cost_service.enums.CostCenterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CostCenterRequest {
    
    @NotBlank(message = "Cost center code is required")
    private String code;
    
    @NotBlank(message = "Cost center name is required")
    private String name;
    
    @NotNull(message = "Cost center type is required")
    private CostCenterType type;
    
    @NotNull(message = "Active status is required")
    private ActiveStatus isActive;
    
    @NotNull(message = "Profit center is required")
    private UUID profitCenterId;
}
