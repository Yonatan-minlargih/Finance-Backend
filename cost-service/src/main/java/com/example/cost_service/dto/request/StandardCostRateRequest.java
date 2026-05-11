package com.example.cost_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
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
public class StandardCostRateRequest {
    
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    @NotBlank(message = "Item code is required")
    private String itemCode;
    
    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rate must be positive")
    @Digits(integer = 19, fraction = 4, message = "Rate must have at most 19 digits and 4 decimal places")
    private BigDecimal rate;
    
    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;
}
