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
public class ArReceiptGlPostResult {

    private boolean success;
    private String message;
    private UUID journalId;
    private String journalNumber;

    public static ArReceiptGlPostResult success(UUID journalId, String journalNumber) {
        return ArReceiptGlPostResult.builder()
                .success(true)
                .message("Receipt posted to General Ledger")
                .journalId(journalId)
                .journalNumber(journalNumber)
                .build();
    }

    public static ArReceiptGlPostResult failure(String message) {
        return ArReceiptGlPostResult.builder().success(false).message(message).build();
    }
}
