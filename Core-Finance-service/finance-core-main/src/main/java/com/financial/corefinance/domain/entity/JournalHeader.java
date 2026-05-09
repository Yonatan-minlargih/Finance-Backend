package com.financial.corefinance.domain.entity;

import com.financial.corefinance.domain.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "journal_headers", indexes = {
    @Index(name = "idx_journal_headers_tenant", columnList = "tenant_id"),
    @Index(name = "idx_journal_headers_number", columnList = "journal_number"),
    @Index(name = "idx_journal_headers_date", columnList = "journal_date"),
    @Index(name = "idx_journal_headers_period", columnList = "accounting_period_id")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uq_journal_headers_tenant_number", columnNames = {"tenant_id", "journal_number"})
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JournalHeader extends BaseEntity {

    @NotBlank(message = "Journal number is required")
    @Column(name = "journal_number", length = 50, nullable = false)
    private String journalNumber;

    @NotNull(message = "Journal date is required")
    @Column(name = "journal_date", nullable = false)
    private LocalDate journalDate;

    @NotNull(message = "Accounting period is required")
    @Column(name = "accounting_period_id", nullable = false)
    private UUID accountingPeriodId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accounting_period_id", insertable = false, updatable = false)
    private AccountingPeriod accountingPeriod;

    @NotNull(message = "Journal type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "journal_type", nullable = false, length = 20)
    private JournalType journalType;

    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @Column(name = "reference_type", length = 20)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @NotBlank(message = "Description is required")
    @Column(name = "description", length = 500, nullable = false)
    private String description;

    @Column(name = "narration", length = 1000)
    private String narration;

    @Builder.Default
    @OneToMany(mappedBy = "journalHeader", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JournalLine> journalLines = new ArrayList<>();

    @Column(name = "total_debit", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalDebit = BigDecimal.ZERO;

    @Column(name = "total_credit", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalCredit = BigDecimal.ZERO;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private JournalStatus status = JournalStatus.DRAFT;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "posted_by", length = 100)
    private String postedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejected_by", length = 100)
    private String rejectedBy;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "is_reversed")
    @Builder.Default
    private Boolean isReversed = false;

    @Column(name = "reversed_by", length = 100)
    private String reversedBy;

    @Column(name = "reversed_at")
    private LocalDateTime reversedAt;

    @Column(name = "reversal_reason", length = 500)
    private String reversalReason;

    @Column(name = "original_journal_id")
    private UUID originalJournalId;

    @Column(name = "auto_reverse")
    @Builder.Default
    private Boolean autoReverse = false;

    @Column(name = "auto_reverse_date")
    private LocalDate autoReverseDate;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "source_system", length = 50)
    @Builder.Default
    private String sourceSystem = "CORE_FINANCE";

    @Column(name = "attachment_count")
    @Builder.Default
    private Integer attachmentCount = 0;

    @PrePersist
    @PreUpdate
    public void calculateTotals() {
        if (journalLines != null) {
            totalDebit = journalLines.stream()
                .filter(line -> line.getDebitAmount() != null)
                .map(JournalLine::getDebitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            totalCredit = journalLines.stream()
                .filter(line -> line.getCreditAmount() != null)
                .map(JournalLine::getCreditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    public enum JournalType {
        MANUAL,
        SYSTEM,
        REVERSAL,
        ADJUSTMENT,
        OPENING_BALANCE,
        CLOSING,
        RECLASSIFICATION
    }

    public enum JournalStatus {
        DRAFT,
        PENDING_APPROVAL,
        APPROVED,
        POSTED,
        REJECTED,
        REVERSED
    }
}
