package com.finance.transactional.service;

import com.finance.transactional.model.ar.SalesInvoice;
import com.finance.transactional.repository.ReceiptAllocationRepository;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArSubledgerBalanceService {

    private static final Set<SalesInvoice.SalesInvoiceStatus> OPEN_STATUSES = EnumSet.of(
            SalesInvoice.SalesInvoiceStatus.ISSUED,
            SalesInvoice.SalesInvoiceStatus.PARTIALLY_PAID,
            SalesInvoice.SalesInvoiceStatus.ON_HOLD);

    private final ReceiptAllocationRepository receiptAllocationRepository;

    public boolean isOpenReceivable(SalesInvoice invoice) {
        return invoice != null
                && invoice.getStatus() != null
                && OPEN_STATUSES.contains(invoice.getStatus());
    }

    public BigDecimal computeAllocated(UUID tenantId, UUID salesInvoiceId, UUID excludeReceiptId) {
        return receiptAllocationRepository.sumAllocatedToSalesInvoice(tenantId, salesInvoiceId, excludeReceiptId);
    }

    public BigDecimal computeBalance(UUID tenantId, SalesInvoice invoice, UUID excludeReceiptId) {
        if (invoice == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal allocated = computeAllocated(tenantId, invoice.getId(), excludeReceiptId);
        return total.subtract(allocated).max(BigDecimal.ZERO);
    }

    public BigDecimal computeOutstanding(UUID tenantId, SalesInvoice invoice, UUID excludeReceiptId) {
        if (invoice == null || invoice.getStatus() == null) {
            return BigDecimal.ZERO;
        }
        if (invoice.getStatus() == SalesInvoice.SalesInvoiceStatus.CANCELLED
                || invoice.getStatus() == SalesInvoice.SalesInvoiceStatus.DRAFT) {
            return BigDecimal.ZERO;
        }
        return computeBalance(tenantId, invoice, excludeReceiptId);
    }
}
