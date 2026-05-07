package com.financial.corefinance.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class IntegrationJournalRequest {

    @NotNull(message = "Journal date is required")
    private LocalDate journalDate;

    @NotNull(message = "Accounting period ID is required")
    private UUID accountingPeriodId;

    @NotBlank(message = "Source system is required")
    private String sourceSystem;

    @NotBlank(message = "Reference number is required")
    private String referenceNumber;

    private String referenceType;
    private UUID referenceId;

    @NotBlank(message = "Description is required")
    private String description;

    private String narration;
    private String batchNumber;

    @Valid
    @NotEmpty(message = "Journal lines are required")
    private List<JournalLineRequest> journalLines;

    @Data
    public static class JournalLineRequest {
        @NotNull(message = "Account ID is required")
        private UUID accountId;

        private BigDecimal debitAmount;
        private BigDecimal creditAmount;

        private String description;
        private UUID costCenterId;
        private UUID departmentId;
        private UUID projectId;
        private String analysisCode;
        private String currencyCode = "USD";
        private BigDecimal exchangeRate = BigDecimal.ONE;
    }
}
