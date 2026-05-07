package com.finance.transactional.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class BankReconciliationDto {
    private UUID id;
    private UUID tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private UUID bankAccountId;
    private LocalDate statementDate;
    private BigDecimal statementBalance;
    private BigDecimal systemBalance;
    private BigDecimal variance;
    private String status;
}
