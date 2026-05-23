package com.financial.corefinance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetLineResponse {
    private UUID id;
    private UUID budgetId;
    private UUID budgetVersionId;
    private UUID accountId;
    private String accountCode;
    private String accountName;
    private UUID departmentId;
    private UUID costCenterId;
    private UUID projectId;
    private Integer periodNumber;
    private BigDecimal budgetAmount;
    private BigDecimal allocatedAmount;
    private BigDecimal actualAmount;
    private BigDecimal commitmentAmount;
    private BigDecimal availableAmount;
    private BigDecimal varianceAmount;
    private BigDecimal variancePercentage;
    private String budgetPeriodType;
    private String spreadMethod;
    private String notes;
    private LocalDate lastUpdatedAt;
}
