package com.financial.corefinance.domain.entity;

import com.financial.corefinance.domain.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "budget_versions", indexes = {
    @Index(name = "idx_budget_versions_tenant", columnList = "tenant_id"),
    @Index(name = "idx_budget_versions_budget", columnList = "budget_id")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uq_budget_versions_budget_number", columnNames = {"budget_id", "version_number"})
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BudgetVersion extends BaseEntity {

    @NotNull(message = "Budget is required")
    @Column(name = "budget_id", nullable = false)
    private UUID budgetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", insertable = false, updatable = false)
    private Budget budget;

    @NotNull(message = "Version number is required")
    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "version_name", length = 100)
    private String versionName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BudgetVersionStatus status = BudgetVersionStatus.DRAFT;

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

    @Column(name = "is_current")
    @Builder.Default
    private Boolean isCurrent = false;

    @Column(name = "is_baseline")
    @Builder.Default
    private Boolean isBaseline = false;

    @Column(name = "approved_at")
    private LocalDate approvedAt;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Builder.Default
    @OneToMany(mappedBy = "budgetVersion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BudgetLine> budgetLines = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "budgetVersion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BudgetChange> budgetChanges = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void calculateVariance() {
        if (totalBudgetAmount != null && totalActualAmount != null) {
            totalVariance = totalBudgetAmount.subtract(totalActualAmount);
        }
    }

    public enum BudgetVersionStatus {
        DRAFT,
        SUBMITTED,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        ACTIVE,
        SUPERSEDED,
        ARCHIVED
    }
}
