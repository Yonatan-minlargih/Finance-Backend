package com.finance.transactional.service;

import com.finance.transactional.model.ap.Invoice;
import com.finance.transactional.repository.PaymentAllocationRepository;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApSubledgerBalanceService {

    private static final Set<Invoice.InvoiceStatus> OPEN_STATUSES = EnumSet.of(
            Invoice.InvoiceStatus.POSTED,
            Invoice.InvoiceStatus.APPROVED,
            Invoice.InvoiceStatus.PARTIALLY_PAID);

    private final PaymentAllocationRepository paymentAllocationRepository;

    public boolean isOpenPayable(Invoice invoice) {
        return invoice != null
                && invoice.getStatus() != null
                && OPEN_STATUSES.contains(invoice.getStatus());
    }

    public BigDecimal computeOutstanding(UUID tenantId, Invoice invoice) {
        if (invoice == null || invoice.getStatus() == null) {
            return BigDecimal.ZERO;
        }
        if (invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED
                || invoice.getStatus() == Invoice.InvoiceStatus.REJECTED
                || invoice.getStatus() == Invoice.InvoiceStatus.DRAFT
                || invoice.getStatus() == Invoice.InvoiceStatus.PENDING_APPROVAL) {
            return BigDecimal.ZERO;
        }
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal allocated = paymentAllocationRepository.sumAllocatedToInvoice(tenantId, invoice.getId());

        return total.subtract(allocated).max(BigDecimal.ZERO);
    }
}
