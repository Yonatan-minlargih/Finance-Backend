package com.financial.corefinance.dto.request;

import com.financial.corefinance.domain.entity.FinancialReport;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class FinancialReportRequest {
    @NotBlank
    private String tenantId;
    @NotNull
    private UUID fiscalYearId;
    @NotBlank
    private String reportName;
    @NotNull
    private FinancialReport.ReportType reportType;
    private String reportPeriod;
    private Integer periodNumber;
    private LocalDate reportDate;
    private LocalDate asOfDate;
    private String currencyCode;
    private String description;
    private UUID templateId;
}
