package com.example.payroll_service.dto.request;

import com.example.payroll_service.enums.CalculationMethod;
import com.example.payroll_service.enums.EarningDeductionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class EarningDeductionTypeRequest {

    @NotBlank(message = "Type name is required")
    private String typeName;

    @NotNull(message = "Is earning is required")
    private Boolean isEarning;

    private Boolean isPercentage;

    private BigDecimal defaultValue;

    private UUID glAccountId;

    private EarningDeductionCategory category;

    private CalculationMethod calculationMethod;

    private Boolean overtimeEligible;

    private LocalDate startDate;

    private LocalDate endDate;
}
