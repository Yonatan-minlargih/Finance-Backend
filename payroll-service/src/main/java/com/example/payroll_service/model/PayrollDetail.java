package com.example.payroll_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payroll_details")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PayrollDetail extends Base {

    @Column(nullable = false)
    private UUID employeeId;

    private BigDecimal basicSalary;

    private BigDecimal overtime;

    private BigDecimal grossSalary;

    private BigDecimal totalDeductions;

    private BigDecimal netSalary;

    private BigDecimal bonusAmount;

    private BigDecimal netToGrossAmount;

    private UUID journalId;

    @ManyToOne
    @JoinColumn(name = "payroll_run_id", nullable = false)
    private PayrollRun payrollRun;
}
