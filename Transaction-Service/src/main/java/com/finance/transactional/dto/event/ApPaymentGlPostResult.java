package com.finance.transactional.dto.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApPaymentGlPostResult {

    private boolean success;
    private String message;
    private UUID journalId;
    private String journalNumber;
    private UUID accountingPeriodId;

    public static ApPaymentGlPostResult success(UUID journalId, String journalNumber, UUID accountingPeriodId) {
        return ApPaymentGlPostResult.builder()
                .success(true)
                .message("Payment posted to General Ledger")
                .journalId(journalId)
                .journalNumber(journalNumber)
                .accountingPeriodId(accountingPeriodId)
                .build();
    }

    public static ApPaymentGlPostResult failure(String message) {
        return ApPaymentGlPostResult.builder().success(false).message(message).build();
    }
}
