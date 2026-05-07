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
    private String currency;
    private Invoice.InvoiceStatus status;
    private List<InvoiceLineDto> lines;
}
