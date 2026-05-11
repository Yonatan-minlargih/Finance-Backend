package com.example.cost_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CostRecordRequest {
    
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    @NotNull(message = "Cost center ID is required")
    private UUID costCenterId;
    
    @NotNull(message = "Material cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Material cost must be positive")
    @Digits(integer = 19, fraction = 4, message = "Material cost must have at most 19 digits and 4 decimal places")
    private BigDecimal materialCost;
    
    @NotNull(message = "Labor cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Labor cost must be positive")
    @Digits(integer = 19, fraction = 4, message = "Labor cost must have at most 19 digits and 4 decimal places")
    private BigDecimal laborCost;
    
    @NotNull(message = "Overhead cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Overhead cost must be positive")
    @Digits(integer = 19, fraction = 4, message = "Overhead cost must have at most 19 digits and 4 decimal places")
    private BigDecimal overheadCost;
    
    @NotNull(message = "Period ID is required")
    private UUID periodId;
}
