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
import java.util.UUID;

@Entity
@Table(name = "accounting_periods", indexes = {
    @Index(name = "idx_accounting_periods_tenant", columnList = "tenant_id"),
    @Index(name = "idx_accounting_periods_fiscal_year", columnList = "fiscal_year_id")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uq_accounting_periods_tenant_fiscal_number", columnNames = {"tenant_id", "fiscal_year_id", "period_number"})
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AccountingPeriod extends BaseEntity {

    @NotNull(message = "Fiscal year is required")
    @Column(name = "fiscal_year_id", nullable = false)
    private UUID fiscalYearId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_year_id", insertable = false, updatable = false)
    private FiscalYear fiscalYear;

    @NotNull(message = "Period number is required")
    @Column(name = "period_number", nullable = false)
    private Integer periodNumber;

    @NotBlank(message = "Period name is required")
    @Column(name = "period_name", length = 50, nullable = false)
    private String periodName;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_open")
    @Builder.Default
    private Boolean isOpen = true;

    @Column(name = "is_closed")
    @Builder.Default
    private Boolean isClosed = false;

    @Column(name = "closed_at")
    private LocalDate closedAt;

    @Column(name = "closed_by", length = 100)
    private String closedBy;

    @Column(name = "is_adjustment_period")
    @Builder.Default
    private Boolean isAdjustmentPeriod = false;

    @Column(name = "description", length = 500)
    private String description;
}
