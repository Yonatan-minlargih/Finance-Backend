package com.financial.corefinance.dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BudgetVsActualLineResponse {
    private UUID budgetLineId;
    private UUID accountId;
    private String accountCode;
    private String accountName;
    private UUID departmentId;
    private UUID costCenterId;
    private String groupKey;
    private String groupLabel;
    private BigDecimal budgetAmount;
    private BigDecimal actualAmount;
    private BigDecimal varianceAmount;
    private BigDecimal variancePercent;
    private BigDecimal utilizationPercent;
    private boolean overBudget;
    private String warningLevel;
}
