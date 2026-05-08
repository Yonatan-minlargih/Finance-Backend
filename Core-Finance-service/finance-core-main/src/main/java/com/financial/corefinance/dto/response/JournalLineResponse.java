package com.financial.corefinance.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class JournalLineResponse {

    private UUID id;
    private String tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
    
    private UUID journalHeaderId;
    private Integer lineNumber;
    private UUID accountId;
    private String accountCode;
    private String accountName;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private String description;
    private UUID costCenterId;
    private UUID departmentId;
    private UUID projectId;
    private UUID productId;
    private UUID locationId;
    private String analysisCode;
    private String referenceNumber;
    private String referenceType;
    private UUID referenceId;
    private String taxCode;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private String currencyCode;
    private BigDecimal exchangeRate;
    private BigDecimal foreignDebitAmount;
    private BigDecimal foreignCreditAmount;
    private Boolean reconciled;
    private LocalDateTime reconciledAt;
    private String reconciledBy;
}
