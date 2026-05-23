package com.financial.corefinance.dto.request;

import com.financial.corefinance.domain.entity.BudgetChange.ChangeType;
import com.financial.corefinance.domain.entity.BudgetChange.ChangeStatus;
import jakarta.validation.constraints.NotBlank;
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
public class BudgetChangeRequest {
    
    private String tenantId;

    @NotNull(message = "Budget version is required")
    private UUID budgetVersionId;

    @NotNull(message = "Budget line is required")
    private UUID budgetLineId;

    @NotNull(message = "Change type is required")
    private ChangeType changeType;

    private BigDecimal oldAmount;

    private BigDecimal newAmount;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String authorityLevel;

    private ChangeStatus status;

    private LocalDate effectiveDate;
}
