package com.financial.corefinance.dto.response;

import com.financial.corefinance.domain.entity.BudgetLine.LineCategory;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BudgetForecastResponse {
    private UUID id;
    private UUID fiscalYearId;
    private UUID departmentId;
    private UUID accountId;
    private String accountCode;
    private String accountName;
    private LineCategory lineCategory;
    private Integer periodNumber;
    private BigDecimal forecastAmount;
    private BigDecimal priorYearActualAmount;
    private String notes;
}
