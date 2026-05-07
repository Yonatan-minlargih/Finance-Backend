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
import java.util.UUID;

@Entity
@Table(name = "budget_changes", indexes = {
    @Index(name = "idx_budget_changes_tenant", columnList = "tenant_id"),
    @Index(name = "idx_budget_changes_version", columnList = "budget_version_id"),
    @Index(name = "idx_budget_changes_line", columnList = "budget_line_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BudgetChange extends BaseEntity {

    @NotNull(message = "Budget version is required")
    @Column(name = "budget_version_id", nullable = false)
    private UUID budgetVersionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_version_id", insertable = false, updatable = false)
    private BudgetVersion budgetVersion;

    @NotNull(message = "Budget line is required")
    @Column(name = "budget_line_id", nullable = false)
    private UUID budgetLineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_line_id", insertable = false, updatable = false)
    private BudgetLine budgetLine;

    @NotNull(message = "Change type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private ChangeType changeType;

    @Column(name = "old_amount", precision = 19, scale = 4)
    private BigDecimal oldAmount;

    @Column(name = "new_amount", precision = 19, scale = 4)
    private BigDecimal newAmount;

    @Column(name = "change_amount", precision = 19, scale = 4)
    private BigDecimal changeAmount;

    @Column(name = "change_percentage", precision = 5, scale = 2)
    private BigDecimal changePercentage;

    @NotBlank(message = "Reason is required")
    @Column(name = "reason", length = 1000, nullable = false)
    private String reason;

    @Column(name = "authority_level", length = 50)
    private String authorityLevel;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private java.time.LocalDateTime approvedAt;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ChangeStatus status = ChangeStatus.PENDING;

    @Column(name = "effective_date")
    private java.time.LocalDate effectiveDate;

    @PrePersist
    public void calculateChange() {
        if (oldAmount != null && newAmount != null) {
            changeAmount = newAmount.subtract(oldAmount);
            
            if (oldAmount.compareTo(BigDecimal.ZERO) != 0) {
                changePercentage = changeAmount.divide(oldAmount, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100));
            }
        }
    }

    public enum ChangeType {
        INCREASE,
        DECREASE,
        TRANSFER_IN,
        TRANSFER_OUT,
        REALLOCATION,
        ADJUSTMENT
    }

    public enum ChangeStatus {
        PENDING,
        APPROVED,
        REJECTED,
        IMPLEMENTED
    }
}
