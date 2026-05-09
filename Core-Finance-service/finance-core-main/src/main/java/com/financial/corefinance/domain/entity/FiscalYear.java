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
@Table(name = "fiscal_years", indexes = {
    @Index(name = "idx_fiscal_years_tenant", columnList = "tenant_id"),
    @Index(name = "idx_fiscal_years_calendar", columnList = "calendar_definition_id")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uq_fiscal_years_tenant_year", columnNames = {"tenant_id", "year_number"})
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FiscalYear extends BaseEntity {

    @NotNull(message = "Year number is required")
    @Column(name = "year_number", nullable = false)
    private Integer yearNumber;

    @NotBlank(message = "Year name is required")
    @Column(name = "year_name", length = 50, nullable = false)
    private String yearName;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull(message = "Calendar definition is required")
    @Column(name = "calendar_definition_id", nullable = false)
    private UUID calendarDefinitionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_definition_id", insertable = false, updatable = false)
    private CalendarDefinition calendarDefinition;

    @Builder.Default
    @OneToMany(mappedBy = "fiscalYear", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccountingPeriod> accountingPeriods = new ArrayList<>();

    @Column(name = "is_current")
    @Builder.Default
    private Boolean isCurrent = false;

    @Column(name = "is_closed")
    @Builder.Default
    private Boolean isClosed = false;

    @Column(name = "closed_at")
    private LocalDate closedAt;

    @Column(name = "closed_by", length = 100)
    private String closedBy;

    @Column(name = "total_periods")
    @Builder.Default
    private Integer totalPeriods = 12;

    @Column(name = "description", length = 500)
    private String description;
}
