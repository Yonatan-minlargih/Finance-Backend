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
public class ApInvoiceGlPostResult {

    private boolean success;
    private String message;
    private UUID journalId;
    private String journalNumber;
    private UUID accountingPeriodId;
    private UUID fiscalYearId;

    public static ApInvoiceGlPostResult success(
            UUID journalId,
            String journalNumber,
            UUID accountingPeriodId,
            UUID fiscalYearId) {
        return ApInvoiceGlPostResult.builder()
                .success(true)
                .message("Journal posted to General Ledger")
                .journalId(journalId)
                .journalNumber(journalNumber)
                .accountingPeriodId(accountingPeriodId)
                .fiscalYearId(fiscalYearId)
                .build();
    }

    public static ApInvoiceGlPostResult failure(String message) {
        return ApInvoiceGlPostResult.builder()
                .success(false)
                .message(message)
                .build();
    }
}
