package com.example.payroll_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
@Entity
@Table(name = "loan_payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LoanPayment extends Base {

    private BigDecimal amount;

    private LocalDate paymentDate;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private EmployeeLoan loan;

    @ManyToOne
    @JoinColumn(name = "payroll_run_id")
    private PayrollRun payrollRun;
}
