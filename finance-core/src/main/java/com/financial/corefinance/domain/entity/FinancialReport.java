package com.financial.corefinance.domain.entity;

import com.financial.corefinance.domain.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "financial_reports", indexes = {
    @Index(name = "idx_financial_reports_tenant", columnList = "tenant_id"),
    @Index(name = "idx_financial_reports_fiscal_year", columnList = "fiscal_year_id"),
    @Index(name = "idx_financial_reports_type", columnList = "report_type")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uq_financial_reports_tenant_fiscal_type_name", columnNames = {"tenant_id", "fiscal_year_id", "report_type", "report_name"})
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FinancialReport extends BaseEntity {

    @NotNull(message = "Fiscal year is required")
    @Column(name = "fiscal_year_id", nullable = false)
    private UUID fiscalYearId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_year_id", insertable = false, updatable = false)
    private FiscalYear fiscalYear;

    @NotBlank(message = "Report name is required")
    @Column(name = "report_name", length = 100, nullable = false)
    private String reportName;

    @NotNull(message = "Report type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

    @Column(name = "report_period", length = 20)
    private String reportPeriod; // ANNUAL, QUARTERLY, MONTHLY

    @Column(name = "period_number")
    private Integer periodNumber;

    @Column(name = "report_date")
    private LocalDate reportDate;

    @Column(name = "as_of_date")
    private LocalDate asOfDate;

    @Column(name = "currency_code", length = 3)
    @Builder.Default
    private String currencyCode = "USD";

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReportStatus status = ReportStatus.DRAFT;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "generated_at")
    private LocalDate generatedAt;

    @Column(name = "generated_by", length = 100)
    private String generatedBy;

    @Column(name = "approved_at")
    private LocalDate approvedAt;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "published_at")
    private LocalDate publishedAt;

    @Column(name = "published_by", length = 100)
    private String publishedBy;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_name", length = 100)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type", length = 20)
    private String fileType;

    @OneToMany(mappedBy = "financialReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReportLine> reportLines = new ArrayList<>();

    public enum ReportType {
        STATEMENT_OF_FINANCIAL_POSITION,
        PROFIT_LOSS_AND_OCI,
        CASH_FLOW_STATEMENT,
        STATEMENT_OF_CHANGES_IN_EQUITY,
        TRIAL_BALANCE,
        GENERAL_LEDGER,
        AGED_TRIAL_BALANCE,
        BUDGET_VARIANCE,
        INCOME_STATEMENT,
        BALANCE_SHEET
    }

    public enum ReportStatus {
        DRAFT,
        GENERATING,
        GENERATED,
        UNDER_REVIEW,
        APPROVED,
        PUBLISHED,
        ARCHIVED
    }
}
