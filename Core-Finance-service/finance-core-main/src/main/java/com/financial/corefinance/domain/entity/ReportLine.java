package com.financial.corefinance.domain.entity;

import com.financial.corefinance.domain.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "report_lines", indexes = {
    @Index(name = "idx_report_lines_tenant", columnList = "tenant_id"),
    @Index(name = "idx_report_lines_report", columnList = "financial_report_id"),
    @Index(name = "idx_report_lines_account", columnList = "account_id")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uq_report_lines_report_line", columnNames = {"financial_report_id", "line_number"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReportLine extends BaseEntity {

    @NotNull(message = "Financial report is required")
    @Column(name = "financial_report_id", nullable = false)
    private UUID financialReportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_report_id", insertable = false, updatable = false)
    private FinancialReport financialReport;

    @NotNull(message = "Line number is required")
    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @Column(name = "account_id")
    private UUID accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private Account account;

    @Column(name = "line_type", length = 20)
    @Builder.Default
    private String lineType = "ACCOUNT"; // ACCOUNT, HEADING, SUBTOTAL, TOTAL

    @Column(name = "line_description", length = 200)
    private String lineDescription;

    @Column(name = "current_period_amount", precision = 19, scale = 4)
    private BigDecimal currentPeriodAmount;

    @Column(name = "prior_period_amount", precision = 19, scale = 4)
    private BigDecimal priorPeriodAmount;

    @Column(name = "ytd_amount", precision = 19, scale = 4)
    private BigDecimal ytdAmount;

    @Column(name = "prior_ytd_amount", precision = 19, scale = 4)
    private BigDecimal priorYtdAmount;

    @Column(name = "budget_amount", precision = 19, scale = 4)
    private BigDecimal budgetAmount;

    @Column(name = "variance_amount", precision = 19, scale = 4)
    private BigDecimal varianceAmount;

    @Column(name = "variance_percentage", precision = 5, scale = 2)
    private BigDecimal variancePercentage;

    @Column(name = "indentation_level")
    @Builder.Default
    private Integer indentationLevel = 0;

    @Column(name = "is_bold")
    @Builder.Default
    private Boolean isBold = false;

    @Column(name = "is_italic")
    @Builder.Default
    private Boolean isItalic = false;

    @Column(name = "show_zero_amounts")
    @Builder.Default
    private Boolean showZeroAmounts = true;

    @Column(name = "calculation_formula", length = 500)
    private String calculationFormula; // For calculated lines

    @Column(name = "sort_order")
    private Integer sortOrder;

    @PrePersist
    @PreUpdate
    public void calculateVariance() {
        if (currentPeriodAmount != null && budgetAmount != null) {
            varianceAmount = currentPeriodAmount.subtract(budgetAmount);
            
            if (budgetAmount.compareTo(BigDecimal.ZERO) != 0) {
                variancePercentage = varianceAmount.divide(budgetAmount, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100));
            }
        }
    }
}
