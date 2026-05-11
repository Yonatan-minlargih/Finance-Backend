package com.example.cost_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfitabilityAnalysisRequest {
    
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    @NotNull(message = "Cost center ID is required")
    private UUID costCenterId;
    
    @NotNull(message = "Profit center ID is required")
    private UUID profitCenterId;
    
    @NotNull(message = "Revenue is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Revenue must be positive")
    @Digits(integer = 19, fraction = 4, message = "Revenue must have at most 19 digits and 4 decimal places")
    private BigDecimal revenue;
    
    @NotNull(message = "COGS is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "COGS must be positive")
    @Digits(integer = 19, fraction = 4, message = "COGS must have at most 19 digits and 4 decimal places")
    private BigDecimal cogs;
    
    @NotNull(message = "Analysis date is required")
    private LocalDate analysisDate;
    
    @NotNull(message = "Period ID is required")
    private UUID periodId;
}
