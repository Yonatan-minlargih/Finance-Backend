package com.financial.corefinance.dto.request;

import com.financial.corefinance.domain.entity.BudgetVersion.BudgetVersionStatus;
import jakarta.validation.constraints.NotNull;
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
public class BudgetVersionRequest {
    
    private String tenantId;

    @NotNull(message = "Budget is required")
    private UUID budgetId;

    @NotNull(message = "Version number is required")
    private Integer versionNumber;

    private String versionName;

    private String description;

    private BudgetVersionStatus status;

    private BigDecimal totalBudgetAmount;

    private Boolean isCurrent;

    private Boolean isBaseline;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}
