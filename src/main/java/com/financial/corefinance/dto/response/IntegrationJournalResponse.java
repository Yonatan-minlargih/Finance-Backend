package com.financial.corefinance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationJournalResponse {

    private Boolean success;
    private UUID journalId;
    private String journalNumber;
    private String status;
    private String message;
    private String referenceNumber;
    private String errorCode;
    private String errorDetails;
}
