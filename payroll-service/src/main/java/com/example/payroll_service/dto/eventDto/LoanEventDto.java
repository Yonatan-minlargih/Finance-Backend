package com.example.payroll_service.dto.eventDto;

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
public class LoanEventDto {

    private UUID id;
    private UUID tenantId;
    private String eventType;
    private UUID employeeId;
    private BigDecimal loanAmount;
    private BigDecimal installment;
    private BigDecimal remainingBalance;
    private BigDecimal amount;
    private String status;
    private LocalDate paymentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
