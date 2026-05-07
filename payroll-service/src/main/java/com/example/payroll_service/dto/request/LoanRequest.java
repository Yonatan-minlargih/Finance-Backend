package com.example.payroll_service.dto.request;

import com.example.payroll_service.enums.LoanStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class LoanRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Loan amount is required")
    @Positive(message = "Loan amount must be positive")
    private BigDecimal loanAmount;

    @NotNull(message = "Installment is required")
    @Positive(message = "Installment must be positive")
    private BigDecimal installment;

    private BigDecimal remainingBalance;

    private BigDecimal interestRate;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean stopPayment;

    private LoanStatus status;
}
