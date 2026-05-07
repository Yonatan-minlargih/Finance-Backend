package com.example.payroll_service.dto.response;

import com.example.payroll_service.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {

    private UUID id;
    private UUID tenantId;
    private UUID employeeId;
    private BigDecimal loanAmount;
    private BigDecimal installment;
    private BigDecimal remainingBalance;
    private BigDecimal interestRate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean stopPayment;
    private LoanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
