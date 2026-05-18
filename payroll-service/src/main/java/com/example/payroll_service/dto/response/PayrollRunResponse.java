package com.example.payroll_service.dto.response;

import com.example.payroll_service.enums.PayrollStatus;
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
public class PayrollRunResponse {

    private UUID id;
    private UUID tenantId;
    private LocalDate runDate;
    private UUID periodId;
    private BigDecimal totalGross;
    private BigDecimal totalNet;
    private PayrollStatus status;
    private UUID journalId;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
