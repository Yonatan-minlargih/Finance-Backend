package com.finance.transactional.dto;

import com.finance.transactional.model.banking.BankTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class BankTransactionDto {
    private UUID id;
    private UUID tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private UUID bankAccountId;
    private LocalDate transactionDate;
    private BankTransaction.TransactionType transactionType;
    private BigDecimal amount;
    private String referenceNumber;
    private String description;
    private Boolean isReconciled;
    private UUID bankReconciliationId;
}
