package com.financial.corefinance.dto.request;

import com.financial.corefinance.domain.entity.JournalHeader;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalHeaderRequest {

    @NotNull(message = "Journal date is required")
    private LocalDate journalDate;

    @NotNull(message = "Accounting period ID is required")
    private UUID accountingPeriodId;

    @NotNull(message = "Journal type is required")
    private JournalHeader.JournalType journalType;

    private String referenceNumber;
    private String referenceType;
    private UUID referenceId;

    @NotBlank(message = "Description is required")
    private String description;

    private String narration;

    @Valid
    @NotNull(message = "Journal lines are required")
    private List<JournalLineRequest> journalLines;

    private Boolean autoReverse = false;
    private LocalDate autoReverseDate;
    private String batchNumber;
    private String sourceSystem = "CORE_FINANCE";
}
