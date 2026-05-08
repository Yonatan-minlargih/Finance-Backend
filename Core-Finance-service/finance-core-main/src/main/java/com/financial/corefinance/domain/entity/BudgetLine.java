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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "budget_lines", indexes = {
    @Index(name = "idx_budget_lines_tenant", columnList = "tenant_id"),
    @Index(name = "idx_budget_lines_budget", columnList = "budget_id"),
    @Index(name = "idx_budget_lines_version", columnList = "budget_version_id"),
    @Index(name = "idx_budget_lines_account", columnList = "account_id")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uq_budget_lines_version_account", columnNames = {"budget_version_id", "account_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BudgetLine extends BaseEntity {

    @NotNull(message = "Budget is required")
    @Column(name = "budget_id", nullable = false)
    private UUID budgetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", insertable = false, updatable = false)
    private Budget budget;

    @Column(name = "budget_version_id")
    private UUID budgetVersionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_version_id", insertable = false, updatable = false)
    private BudgetVersion budgetVersion;

    @NotNull(message = "Account is required")
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private Account account;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "cost_center_id")
    private UUID costCenterId;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "period_number")
    private Integer periodNumber;

    @Column(name = "budget_amount", precision = 19, scale = 4)
    private BigDecimal budgetAmount;

    @Column(name = "allocated_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal allocatedAmount = BigDecimal.ZERO;

    @Column(name = "actual_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal actualAmount = BigDecimal.ZERO;

    @Column(name = "commitment_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal commitmentAmount = BigDecimal.ZERO;

    @Column(name = "available_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal availableAmount = BigDecimal.ZERO;

    @Column(name = "variance_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal varianceAmount = BigDecimal.ZERO;

    @Column(name = "variance_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal variancePercentage = BigDecimal.ZERO;

    @Column(name = "budget_period_type", length = 20)
    @Builder.Default
    private String budgetPeriodType = "ANNUAL"; // ANNUAL, QUARTERLY, MONTHLY

    @Column(name = "spread_method", length = 20)
    @Builder.Default
    private String spreadMethod = "EVEN"; // EVEN, MANUAL, PERCENTAGE

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "last_updated_at")
    private LocalDate lastUpdatedAt;

    @PrePersist
    @PreUpdate
    public void calculateAmounts() {
        if (budgetAmount != null) {
            availableAmount = budgetAmount.subtract(allocatedAmount).subtract(actualAmount).subtract(commitmentAmount);
            
            if (budgetAmount.compareTo(BigDecimal.ZERO) != 0) {
                varianceAmount = budgetAmount.subtract(actualAmount);
                variancePercentage = varianceAmount.divide(budgetAmount, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100));
            }
        }
        
        lastUpdatedAt = LocalDate.now();
    }
}
