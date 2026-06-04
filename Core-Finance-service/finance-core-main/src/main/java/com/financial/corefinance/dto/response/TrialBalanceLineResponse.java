package com.financial.corefinance.dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrialBalanceLineResponse {
    private UUID accountId;
    private String accountCode;
    private String accountName;
    private String accountType;
    private String normalBalance;
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal balance;
    private boolean integrationAccount;
    private boolean abnormalBalance;
    private String abnormalBalanceReason;
}
