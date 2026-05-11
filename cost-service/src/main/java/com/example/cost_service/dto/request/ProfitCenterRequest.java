package com.example.cost_service.dto.request;

import com.example.cost_service.enums.ActiveStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfitCenterRequest {
    
    @NotBlank(message = "Profit center code is required")
    private String code;
    
    @NotBlank(message = "Profit center name is required")
    private String name;
    
    @NotNull(message = "Active status is required")
    private ActiveStatus isActive;
}
