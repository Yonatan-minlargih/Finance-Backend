package com.financial.corefinance.dto.response;

import com.financial.corefinance.domain.entity.BudgetVersion.BudgetVersionStatus;
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
public class BudgetVersionResponse {
    private UUID id;
    private String tenantId;
    private UUID budgetId;
    private String budgetName;
    private Integer versionNumber;
    private String versionName;
    private String description;
    private BudgetVersionStatus status;
    private BigDecimal totalBudgetAmount;
    private BigDecimal totalAllocatedAmount;
    private BigDecimal totalActualAmount;
    private BigDecimal totalVariance;
    private Boolean isCurrent;
    private Boolean isBaseline;
    private LocalDate approvedAt;
    private String approvedBy;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
}
