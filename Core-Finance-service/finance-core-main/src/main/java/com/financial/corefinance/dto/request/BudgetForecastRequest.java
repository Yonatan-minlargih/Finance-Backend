package com.financial.corefinance.dto.request;

import com.financial.corefinance.domain.entity.BudgetLine.LineCategory;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class BudgetForecastRequest {

    @NotNull
    private UUID fiscalYearId;

    private UUID departmentId;

    private UUID accountId;

    @NotNull
    private LineCategory lineCategory;

    @NotNull
    private Integer periodNumber;

    @NotNull
    private BigDecimal forecastAmount;

    private BigDecimal priorYearActualAmount;

    private String notes;
}
