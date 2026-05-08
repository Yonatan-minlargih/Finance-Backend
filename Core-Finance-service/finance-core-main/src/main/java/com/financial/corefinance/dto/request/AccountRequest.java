package com.financial.corefinance.dto.request;

import com.financial.corefinance.domain.entity.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class AccountRequest {
    @NotBlank
    private String tenantId;
    @NotBlank
    private String accountCode;
    @NotBlank
    private String accountName;
    @NotNull
    private Account.AccountType accountType;
    @NotNull
    private Account.NormalBalance normalBalance;
    private UUID parentAccountId;
    private Account.IFRSCategory ifrsCategory;
    private String description;
    private Boolean isActive;
    private Boolean isConsolidated;
    private String accountFormat;
    private Integer minLength;
    private Integer maxLength;
    private Boolean allowManualEntry;
    private Boolean reconciliationRequired;
    private BigDecimal openingBalance;
    private LocalDate openingBalanceDate;
    private String currencyCode;
}