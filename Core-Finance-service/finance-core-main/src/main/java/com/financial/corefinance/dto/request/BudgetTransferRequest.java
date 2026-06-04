package com.financial.corefinance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class BudgetTransferRequest {

    @NotNull
    private UUID budgetVersionId;

    @NotNull
    private UUID fromBudgetLineId;

    @NotNull
    private UUID toBudgetLineId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String reason;

    private String authorityLevel;

    private boolean autoApprove = true;
}
