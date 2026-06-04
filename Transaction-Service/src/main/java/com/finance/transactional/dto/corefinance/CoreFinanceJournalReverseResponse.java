package com.finance.transactional.dto.corefinance;

import java.util.UUID;
import lombok.Data;

@Data
public class CoreFinanceJournalReverseResponse {
    private Boolean success;
    private UUID journalId;
    private String journalNumber;
    private String status;
    private String message;
}
