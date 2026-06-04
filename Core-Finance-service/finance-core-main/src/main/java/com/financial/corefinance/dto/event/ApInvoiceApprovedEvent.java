package com.financial.corefinance.dto.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApInvoiceApprovedEvent {

    private UUID id;
    private UUID tenantId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private String currency;
    private BigDecimal foreignTotalAmount;
    private BigDecimal exchangeRate;
    private String invoiceType;
    private UUID vendorId;
    private List<ApInvoiceLineEvent> lines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApInvoiceLineEvent {
        private UUID id;
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineAmount;
        /** GL account UUID or account code (e.g. 5100, 6100). */
        private String accountId;
    }
}
