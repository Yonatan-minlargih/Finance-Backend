package com.example.payroll_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PayrollProcessRequest {

    @NotNull(message = "Payroll run ID is required")
    private UUID payrollRunId;
}
