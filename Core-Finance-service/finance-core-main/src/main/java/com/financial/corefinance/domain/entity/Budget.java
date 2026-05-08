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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "budgets", indexes = {
    @Index(name = "idx_budgets_tenant", columnList = "tenant_id"),
    @Index(name = "idx_budgets_fiscal_year", columnList = "fiscal_year_id"),
    @Index(name = "idx_budgets_department", columnList = "department_id")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uq_budgets_tenant_fiscal_year_name", columnNames = {"tenant_id", "fiscal_year_id", "budget_name"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Budget extends BaseEntity {

    @NotNull(message = "Fiscal year is required")
    @Column(name = "fiscal_year_id", nullable = false)
    private UUID fiscalYearId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_year_id", insertable = false, updatable = false)
    private FiscalYear fiscalYear;

    @NotBlank(message = "Budget name is required")
    @Column(name = "budget_name", length = 100, nullable = false)
    private String budgetName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "budget_type")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BudgetType budgetType = BudgetType.OPERATING;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BudgetStatus status = BudgetStatus.DRAFT;

    @Column(name = "currency_code", length = 3)
    @Builder.Default
    private String currencyCode = "USD";

    @Column(name = "total_budget_amount", precision = 19, scale = 4)
    private BigDecimal totalBudgetAmount;

    @Column(name = "total_allocated_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalAllocatedAmount = BigDecimal.ZERO;

    @Column(name = "total_actual_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalActualAmount = BigDecimal.ZERO;

    @Column(name = "total_variance", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalVariance = BigDecimal.ZERO;

    @Column(name = "approval_required")
    @Builder.Default
    private Boolean approvalRequired = true;

    @Column(name = "approved_at")
    private LocalDate approvedAt;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "locked")
    @Builder.Default
    private Boolean locked = false;

    @Column(name = "locked_at")
    private LocalDate lockedAt;

    @Column(name = "locked_by", length = 100)
    private String lockedBy;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;



    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BudgetVersion> budgetVersions = new ArrayList<>();

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BudgetLine> budgetLines = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void calculateVariance() {
        if (totalBudgetAmount != null && totalActualAmount != null) {
            totalVariance = totalBudgetAmount.subtract(totalActualAmount);
        }
    }

    public enum BudgetType {
        OPERATING,
        CAPITAL,
        CASH_FLOW,
        MASTER,
        DEPARTMENTAL,
        PROJECT
    }

    public enum BudgetStatus {
        DRAFT,
        SUBMITTED,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        ACTIVE,
        CLOSED,
        ARCHIVED
    }
}
