package com.example.payroll_service.dto.request;

import com.example.payroll_service.enums.PayrollStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class PayrollRunRequest {

    @NotNull(message = "Run date is required")
    private LocalDate runDate;

    @NotBlank(message = "Fiscal period is required")
    private String fiscalPeriod;

    private BigDecimal totalGross;

    private BigDecimal totalNet;

    @NotNull(message = "Status is required")
    private PayrollStatus status;

    private UUID journalId;

    private LocalDate periodStartDate;

    private LocalDate periodEndDate;
}
