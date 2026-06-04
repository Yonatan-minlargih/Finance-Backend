package com.financial.corefinance.dto.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArSalesInvoiceGlPostResult {

    private boolean success;
    private String message;
    private UUID journalId;
    private String journalNumber;
    private UUID accountingPeriodId;

    public static ArSalesInvoiceGlPostResult success(UUID journalId, String journalNumber, UUID accountingPeriodId) {
        return ArSalesInvoiceGlPostResult.builder()
                .success(true)
                .message("Journal posted to General Ledger")
                .journalId(journalId)
                .journalNumber(journalNumber)
                .accountingPeriodId(accountingPeriodId)
                .build();
    }

    public static ArSalesInvoiceGlPostResult failure(String message) {
        return ArSalesInvoiceGlPostResult.builder().success(false).message(message).build();
    }
}
