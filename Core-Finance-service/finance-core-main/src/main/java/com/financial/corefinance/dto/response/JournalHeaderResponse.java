package com.financial.corefinance.dto.response;

import com.financial.corefinance.domain.entity.JournalHeader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalHeaderResponse {

    private UUID journalId;
    private String tenantId;
    private String journalNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
    
    private LocalDate journalDate;
    private UUID accountingPeriodId;
    private String accountingPeriodName;
    private JournalHeader.JournalType journalType;
    private String referenceNumber;
    private String referenceType;
    private UUID referenceId;
    private String description;
    private String narration;
    private List<JournalLineResponse> journalLines;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private JournalHeader.JournalStatus status;
    private LocalDateTime postedAt;
    private String postedBy;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private LocalDateTime rejectedAt;
    private String rejectedBy;
    private String rejectionReason;
    private Boolean isReversed;
    private String reversedBy;
    private LocalDateTime reversedAt;
    private String reversalReason;
    private UUID originalJournalId;
    private Boolean autoReverse;
    private LocalDate autoReverseDate;
    private String batchNumber;
    private String sourceSystem;
    private Integer attachmentCount;
}
