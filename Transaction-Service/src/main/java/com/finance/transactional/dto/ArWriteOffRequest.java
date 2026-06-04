package com.finance.transactional.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ArWriteOffRequest {
    @NotNull
    @Positive
    private BigDecimal amount;

    private String reason;

    private LocalDate adjustmentDate;
}
