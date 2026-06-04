package com.financial.corefinance.dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BudgetMultiYearColumnResponse {
    private UUID fiscalYearId;
    private String fiscalYearLabel;
    private BigDecimal budgetAmount;
    private BigDecimal actualAmount;
    private BigDecimal varianceAmount;
}
