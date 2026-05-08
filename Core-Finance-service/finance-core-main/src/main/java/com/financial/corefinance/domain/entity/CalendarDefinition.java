package com.financial.corefinance.domain.entity;

import com.financial.corefinance.domain.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "calendar_definitions", indexes = {
    @Index(name = "idx_calendar_definitions_tenant", columnList = "tenant_id")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uq_calendar_definitions_tenant_name", columnNames = {"tenant_id", "calendar_name"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CalendarDefinition extends BaseEntity {

    @NotBlank(message = "Calendar name is required")
    @Column(name = "calendar_name", length = 100, nullable = false)
    private String calendarName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "period_type", length = 20)
    @Builder.Default
    private String periodType = "MONTHLY"; // MONTHLY, QUARTERLY, YEARLY

    @Column(name = "year_start_month")
    @Builder.Default
    private Integer yearStartMonth = 1; // January = 1

    @Column(name = "year_start_day")
    @Builder.Default
    private Integer yearStartDay = 1;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;
}
