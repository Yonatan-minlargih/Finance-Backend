package com.example.payroll_service.dto.response;

import com.example.payroll_service.enums.CalculationMethod;
import com.example.payroll_service.enums.EarningDeductionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningDeductionTypeResponse {

    private UUID id;
    private UUID tenantId;
    private String typeName;
    private Boolean isEarning;
    private Boolean isPercentage;
    private BigDecimal defaultValue;
    private UUID glAccountId;
    private EarningDeductionCategory category;
    private CalculationMethod calculationMethod;
    private Boolean overtimeEligible;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
