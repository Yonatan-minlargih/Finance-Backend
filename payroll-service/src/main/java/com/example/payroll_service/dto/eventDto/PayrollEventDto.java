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
public class PayrollEventDto {

    private UUID id;
    private UUID tenantId;
    private String eventType;
    private UUID employeeId;
    private LocalDate runDate;
    private String fiscalPeriod;
    private BigDecimal totalGross;
    private BigDecimal totalNet;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for different event types
    private LocalDateTime approvedAt;
    private LocalDateTime cancelledAt;
    private BigDecimal amount;
    private UUID journalId;
    private UUID payrollRunId;
    private UUID loanId;
    private UUID typeId;
}
