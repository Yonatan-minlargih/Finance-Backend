package com.example.payroll_service.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SalaryComponentRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Type ID is required")
    private UUID typeId;

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
}
