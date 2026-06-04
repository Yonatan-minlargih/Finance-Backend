package com.financial.corefinance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BudgetReportResponse {
    private UUID budgetId;
    private String budgetName;
    private UUID budgetVersionId;
    private String versionName;
    private UUID fiscalYearId;
    private UUID departmentId;
    private LocalDate asOfDate;
    private String groupBy;
    private BigDecimal totalBudget;
    private BigDecimal totalActual;
    private BigDecimal totalVariance;
    private BigDecimal totalUtilizationPercent;
    private List<BudgetVsActualLineResponse> lines;
    /** Human-readable hint when monitoring cannot run (e.g. no current version). */
    private String monitoringNote;
}
