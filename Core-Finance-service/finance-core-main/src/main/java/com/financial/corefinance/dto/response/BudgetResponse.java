package com.financial.corefinance.dto.response;

import com.financial.corefinance.domain.entity.Budget;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class BudgetResponse {
    private UUID id;
    private String tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
    private UUID fiscalYearId;
    private String budgetName;
    private String description;
    private UUID departmentId;
    private Budget.BudgetType budgetType;
    private Budget.BudgetStatus status;
    private String currencyCode;
    private BigDecimal totalBudgetAmount;
    private BigDecimal totalAllocatedAmount;
    private BigDecimal totalActualAmount;
    private BigDecimal totalVariance;
    private Boolean approvalRequired;
    private LocalDate approvedAt;
    private String approvedBy;
    private Boolean locked;
    private LocalDate lockedAt;
    private String lockedBy;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}