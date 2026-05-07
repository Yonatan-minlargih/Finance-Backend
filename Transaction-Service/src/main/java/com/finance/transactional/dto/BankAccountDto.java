package com.finance.transactional.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class BankAccountDto {
    private UUID id;
    private UUID tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private String accountCode;
    private String bankName;
    private String accountNumber;
    private String currency;
    private BigDecimal currentBalance;
    private Boolean isActive;
    private String glAccountId;
}
