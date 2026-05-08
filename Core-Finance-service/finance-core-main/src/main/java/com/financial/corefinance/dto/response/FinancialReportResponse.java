package com.financial.corefinance.dto.response;

import com.financial.corefinance.domain.entity.FinancialReport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class FinancialReportResponse {
    private UUID id;
    private String tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
    private UUID fiscalYearId;
    private String reportName;
    private FinancialReport.ReportType reportType;
    private String reportPeriod;
    private Integer periodNumber;
    private LocalDate reportDate;
    private LocalDate asOfDate;
    private String currencyCode;
    private FinancialReport.ReportStatus status;
    private String description;
    private UUID templateId;
    private LocalDate generatedAt;
    private String generatedBy;
    private LocalDate approvedAt;
    private String approvedBy;
    private LocalDate publishedAt;
    private String publishedBy;
}
