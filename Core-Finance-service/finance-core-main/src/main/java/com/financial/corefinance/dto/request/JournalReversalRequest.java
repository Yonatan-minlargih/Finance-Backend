package com.financial.corefinance.dto.request;

import lombok.Data;

@Data
public class JournalReversalRequest {
    /** Frontend sends { "reason": "..." } */
    private String reason;
    /** Alternate field name */
    private String reversalReason;
}
