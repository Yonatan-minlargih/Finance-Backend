package com.financial.corefinance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class JournalLineRequest {

    @NotNull(message = "Account ID is required")
    private UUID accountId;

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
    private String currencyCode = "USD";
    private BigDecimal exchangeRate = BigDecimal.ONE;
    private BigDecimal foreignDebitAmount;
    private BigDecimal foreignCreditAmount;
}
