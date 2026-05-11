package com.example.cost_service.dto.request;

import com.example.cost_service.enums.ApplicableTo;
import com.example.cost_service.enums.TaxType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithholdingTaxRuleRequest {
    
    @NotBlank(message = "Tax name is required")
    private String taxName;
    
    @NotNull(message = "Tax type is required")
    private TaxType taxType;
    
    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rate must be positive")
    @Digits(integer = 19, fraction = 4, message = "Rate must have at most 19 digits and 4 decimal places")
    private BigDecimal rate;
    
    @NotNull(message = "Applicable to is required")
    private ApplicableTo applicableTo;
}
