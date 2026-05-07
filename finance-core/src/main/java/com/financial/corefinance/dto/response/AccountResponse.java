package com.financial.corefinance.dto.response;

import com.financial.corefinance.domain.entity.Account;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class AccountResponse {
    private UUID id;
    private String tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
    private String accountCode;
    private String accountName;
    private Account.AccountType accountType;
    private Account.NormalBalance normalBalance;
    private UUID parentAccountId;
    private String description;
    private Boolean isActive;
    private Boolean isConsolidated;
    private Integer level;
    private String accountFormat;
    private Integer minLength;
    private Integer maxLength;
    private Boolean allowManualEntry;
    private Boolean reconciliationRequired;
    private BigDecimal openingBalance;
    private LocalDate openingBalanceDate;
    private String currencyCode;
    private Account.IFRSCategory ifrsCategory;
}