package com.example.payroll_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollDetailResponse {

    private UUID id;
    private UUID tenantId;
    private UUID employeeId;
    private BigDecimal basicSalary;
    private BigDecimal overtime;
    private BigDecimal grossSalary;
    private BigDecimal totalDeductions;
    private BigDecimal netSalary;
    private BigDecimal bonusAmount;
    private BigDecimal netToGrossAmount;
    private UUID journalId;
    private UUID payrollRunId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
