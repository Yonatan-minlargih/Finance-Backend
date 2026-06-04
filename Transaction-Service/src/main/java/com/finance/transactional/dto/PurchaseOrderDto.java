package com.finance.transactional.dto;

import com.finance.transactional.model.ap.PurchaseOrder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class PurchaseOrderDto {
    private UUID id;
    private UUID tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private String poNumber;
    private UUID vendorId;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private BigDecimal totalAmount;
    private String currency;
    private PurchaseOrder.POStatus status;

    /** Sum of AP invoice totals linked to this PO (computed). */
    private BigDecimal invoicedAmount;

    /** Sum of payments linked to this PO (computed). */
    private BigDecimal paidAmount;

    /** PO total minus invoiced (remaining to invoice). */
    private BigDecimal remainingBalance;

    /** PO total minus paid (remaining to pay). */
    private BigDecimal remainingPaymentBalance;
}
