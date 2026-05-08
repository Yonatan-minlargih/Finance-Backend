package com.financial.corefinance.dto.request;

import com.financial.corefinance.domain.entity.Budget;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class BudgetRequest {
    @NotBlank
    private String tenantId;
    @NotNull
    private UUID fiscalYearId;
    @NotBlank
    private String budgetName;
    private String description;
    private UUID departmentId;
    private Budget.BudgetType budgetType;
    private Budget.BudgetStatus status;
    private String currencyCode;
    private BigDecimal totalBudgetAmount;
    private Boolean approvalRequired;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}