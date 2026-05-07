package com.example.payroll_service.model;

import com.example.payroll_service.enums.LoanStatus;
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
@Table(name = "employee_loans")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmployeeLoan extends Base {

    @Column(nullable = false)
    private UUID employeeId;

    private BigDecimal loanAmount;

    private BigDecimal installment;

    private BigDecimal remainingBalance;

    private BigDecimal interestRate;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(columnDefinition = "TINYINT(1)")
    private Boolean stopPayment;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    @OneToMany(mappedBy = "loan")
    private Set<LoanPayment> loanPayments = new HashSet<>();
}
