package com.finance.transactional.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceAuditTrailDto {

    private UUID invoiceId;
    private String invoiceNumber;
    private String status;
    private AuditStep created;
    private AuditStep approved;
    private AuditStep posted;
    private AuditStep voided;
    private GlJournalRef glJournal;
    private GlJournalRef glReversalJournal;

    @Data
    @Builder
    public static class AuditStep {
        private String by;
        private LocalDateTime at;
    }

    @Data
    @Builder
    public static class GlJournalRef {
        private UUID id;
        private String number;
    }
}
