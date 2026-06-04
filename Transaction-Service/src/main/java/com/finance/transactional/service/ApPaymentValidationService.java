package com.finance.transactional.service;

import com.finance.transactional.model.ap.Invoice;
import com.finance.transactional.model.ap.Payment;
import com.finance.transactional.model.ap.PaymentAllocation;
import com.finance.transactional.repository.InvoiceRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApPaymentValidationService {

    private final InvoiceRepository invoiceRepository;

    /**
     * Payment GL posting requires at least one supplier bill already accrued to GL
     * (posted AP invoice with glJournalId), or explicit allocations to such invoices.
     */
    public void validatePaymentCanPostToGl(Payment payment) {
        UUID tenantId = payment.getTenantId();
        UUID vendorId = payment.getVendor() != null ? payment.getVendor().getId() : null;
        if (vendorId == null) {
            throw new IllegalArgumentException("Payment must have a vendor");
        }

        if (payment.getAllocations() != null && !payment.getAllocations().isEmpty()) {
            for (PaymentAllocation allocation : payment.getAllocations()) {
                if (allocation.getInvoice() == null || allocation.getInvoice().getId() == null) {
                    continue;
                }
                Invoice invoice = invoiceRepository
                        .findByTenantIdAndId(tenantId, allocation.getInvoice().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Allocated AP invoice not found"));
                assertInvoicePostedToGl(invoice);
            }
            return;
        }

        boolean hasPostedBill = invoiceRepository.existsByTenantIdAndVendor_IdAndStatusAndGlJournalIdIsNotNull(
                tenantId, vendorId, Invoice.InvoiceStatus.POSTED);
        if (!hasPostedBill) {
            throw new IllegalStateException(
                    "Cannot post payment: no approved supplier bills for this vendor in GL. "
                            + "Approve an AP invoice first (Dr Expense / Cr Accounts Payable).");
        }
    }

    private static void assertInvoicePostedToGl(Invoice invoice) {
        if (invoice.getStatus() != Invoice.InvoiceStatus.POSTED) {
            throw new IllegalStateException(
                    "Invoice " + invoice.getInvoiceNumber() + " must be approved/posted before payment.");
        }
        if (invoice.getGlJournalId() == null) {
            throw new IllegalStateException(
                    "Invoice " + invoice.getInvoiceNumber() + " has no GL accrual journal. Approve the bill first.");
        }
    }
}
