package com.example.payroll_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "employee_salary_components")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmployeeSalaryComponent extends Base {

    @Column(nullable = false)
    private UUID employeeId;

    private BigDecimal amount;

    @Column(columnDefinition = "TINYINT(1)")
    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private EarningDeductionType type;
}
