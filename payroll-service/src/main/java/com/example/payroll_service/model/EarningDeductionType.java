package com.example.payroll_service.model;

import com.example.payroll_service.enums.CalculationMethod;
import com.example.payroll_service.enums.EarningDeductionCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "earnings_deduction_types")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EarningDeductionType extends Base {

    @Column(nullable = false)
    private String typeName;

    @Column(columnDefinition = "TINYINT(1)")
    private Boolean isEarning;

    @Column(columnDefinition = "TINYINT(1)")
    private Boolean isPercentage;

    private BigDecimal defaultValue;

    private UUID glAccountId;

    @Enumerated(EnumType.STRING)
    private EarningDeductionCategory category;

    @Enumerated(EnumType.STRING)
    private CalculationMethod calculationMethod;

    @Column(columnDefinition = "TINYINT(1)")
    private Boolean overtimeEligible;

    private LocalDate startDate;

    private LocalDate endDate;

    @OneToMany(mappedBy = "type")
    private Set<EmployeeSalaryComponent> components = new HashSet<>();
}
