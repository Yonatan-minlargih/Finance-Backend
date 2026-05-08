package com.financial.corefinance.domain.entity;

import com.financial.corefinance.domain.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "journal_lines", indexes = {
    @Index(name = "idx_journal_lines_tenant", columnList = "tenant_id"),
    @Index(name = "idx_journal_lines_header", columnList = "journal_header_id"),
    @Index(name = "idx_journal_lines_account", columnList = "account_id")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uq_journal_lines_header_line", columnNames = {"journal_header_id", "line_number"})
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JournalLine extends BaseEntity {

    @NotNull(message = "Journal header is required")
    @Column(name = "journal_header_id", nullable = false)
    private UUID journalHeaderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_header_id", insertable = false, updatable = false)
    private JournalHeader journalHeader;

    @NotNull(message = "Line number is required")
    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @NotNull(message = "Account is required")
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private Account account;

    @Column(name = "debit_amount", precision = 19, scale = 4)
    private BigDecimal debitAmount;

    @Column(name = "credit_amount", precision = 19, scale = 4)
    private BigDecimal creditAmount;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "cost_center_id")
    private UUID costCenterId;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "analysis_code", length = 50)
    private String analysisCode;

    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @Column(name = "reference_type", length = 20)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "tax_code", length = 20)
    private String taxCode;

    @Column(name = "tax_rate", precision = 5, scale = 4)
    private BigDecimal taxRate;

    @Column(name = "tax_amount", precision = 19, scale = 4)
    private BigDecimal taxAmount;

    @Column(name = "currency_code", length = 3)
    @Builder.Default
    private String currencyCode = "USD";

    @Column(name = "exchange_rate", precision = 19, scale = 8)
    @Builder.Default
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(name = "foreign_debit_amount", precision = 19, scale = 4)
    private BigDecimal foreignDebitAmount;

    @Column(name = "foreign_credit_amount", precision = 19, scale = 4)
    private BigDecimal foreignCreditAmount;

    @Column(name = "reconciled")
    @Builder.Default
    private Boolean reconciled = false;

    @Column(name = "reconciled_at")
    private java.time.LocalDateTime reconciledAt;

    @Column(name = "reconciled_by", length = 100)
    private String reconciledBy;

    @PrePersist
    @PreUpdate
    public void validateAmounts() {
        if (debitAmount != null && creditAmount != null) {
            if (debitAmount.compareTo(BigDecimal.ZERO) > 0 && creditAmount.compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalArgumentException("Journal line cannot have both debit and credit amounts");
            }
        }
        
        if ((debitAmount == null || debitAmount.compareTo(BigDecimal.ZERO) == 0) && 
            (creditAmount == null || creditAmount.compareTo(BigDecimal.ZERO) == 0)) {
            throw new IllegalArgumentException("Journal line must have either debit or credit amount");
        }
    }
}
