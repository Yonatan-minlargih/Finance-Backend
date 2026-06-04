package com.finance.transactional.model.ap;

import com.finance.transactional.model.BaseTenantEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice extends BaseTenantEntity {

    @Column(name = "invoice_number", length = 50, nullable = false)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id")
    private PurchaseOrder purchaseOrder;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount;

    /** Expense base (sum of distribution lines) before VAT. */
    @Column(name = "subtotal_amount", precision = 15, scale = 2)
    private BigDecimal subtotalAmount;

    /** Ethiopian standard VAT rate (e.g. 15). */
    @Column(name = "vat_rate", precision = 7, scale = 4)
    private BigDecimal vatRate;

    @Column(name = "vendor_tax_id", length = 50)
    private String vendorTaxId;

    @Column(name = "vendor_vat_number", length = 50)
    private String vendorVatNumber;

    @Column(name = "gl_accounting_period_id")
    private UUID glAccountingPeriodId;

    @Column(name = "gl_fiscal_year_id")
    private UUID glFiscalYearId;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "foreign_total_amount", precision = 15, scale = 2)
    private BigDecimal foreignTotalAmount;

    @Column(name = "exchange_rate", precision = 19, scale = 8)
    private BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private InvoiceStatus status;

    @Column(name = "gl_journal_id")
    private UUID glJournalId;

    @Column(name = "gl_journal_number", length = 50)
    private String glJournalNumber;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "posted_by", length = 100)
    private String postedBy;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "voided_by", length = 100)
    private String voidedBy;

    @Column(name = "voided_at")
    private LocalDateTime voidedAt;

    @Column(name = "void_reason", length = 500)
    private String voidReason;

    @Column(name = "gl_reversal_journal_id")
    private UUID glReversalJournalId;

    @Column(name = "gl_reversal_journal_number", length = 50)
    private String glReversalJournalNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type", length = 50)
    private InvoiceType invoiceType;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<InvoiceLine> lines = new ArrayList<>();

    public enum InvoiceStatus {
        DRAFT, PENDING_APPROVAL, APPROVED, POSTED, REJECTED, PAID, PARTIALLY_PAID, CANCELLED, ON_HOLD
    }

    public enum InvoiceType {
        STANDARD, CREDIT_MEMO, DEBIT_MEMO, EXPENSE_REPORT
    }
}
