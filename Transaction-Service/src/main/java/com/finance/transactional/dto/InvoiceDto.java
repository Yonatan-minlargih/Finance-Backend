package com.finance.transactional.dto;

import com.finance.transactional.model.ap.Invoice;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class InvoiceDto {
    private UUID id;
    private UUID tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private String invoiceNumber;
    private UUID vendorId;
    private UUID purchaseOrderId;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal subtotalAmount;
    private BigDecimal vatRate;
    private String vendorTaxId;
    private String vendorVatNumber;
    private UUID glAccountingPeriodId;
    private UUID glFiscalYearId;
    private String currency;
    private BigDecimal foreignTotalAmount;
    private BigDecimal exchangeRate;
    private Invoice.InvoiceStatus status;
    private Invoice.InvoiceType invoiceType;
    private List<InvoiceLineDto> lines;
    private UUID glJournalId;
    private String glJournalNumber;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String postedBy;
    private LocalDateTime postedAt;
    private String voidedBy;
    private LocalDateTime voidedAt;
    private String voidReason;
    private UUID glReversalJournalId;
    private String glReversalJournalNumber;
}
