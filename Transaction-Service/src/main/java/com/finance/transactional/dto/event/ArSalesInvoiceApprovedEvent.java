package com.finance.transactional.dto.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArSalesInvoiceApprovedEvent {
    private UUID id;
    private UUID tenantId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private BigDecimal totalAmount;
}
