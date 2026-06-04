package com.finance.transactional.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ArInterestRequest {
    @NotNull
    @Positive
    private BigDecimal interestAmount;

    private String reason;

    private LocalDate assessmentDate;
}
