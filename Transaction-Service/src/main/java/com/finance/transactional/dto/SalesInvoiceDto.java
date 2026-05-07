package com.finance.transactional.dto;

import com.finance.transactional.model.ar.SalesInvoice;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class SalesInvoiceDto {
    private UUID id;
    private UUID tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private String invoiceNumber;
    private UUID customerId;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private String currency;
    private SalesInvoice.SalesInvoiceStatus status;
}
