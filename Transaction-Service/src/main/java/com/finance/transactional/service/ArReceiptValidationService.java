package com.finance.transactional.service;

import com.finance.transactional.dto.ReceiptAllocationDto;
import com.finance.transactional.model.ar.Receipt;
import com.finance.transactional.model.ar.SalesInvoice;
import com.finance.transactional.repository.SalesInvoiceRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArReceiptValidationService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final ArSubledgerBalanceService subledgerBalanceService;

    public void validateReceiptCanPost(Receipt receipt, List<ReceiptAllocationDto> requestedAllocations) {
        UUID tenantId = receipt.getTenantId();
        UUID customerId = receipt.getCustomer() != null ? receipt.getCustomer().getId() : null;
        if (customerId == null) {
            throw new IllegalArgumentException("Receipt must have a customer");
        }

        List<ReceiptAllocationDto> allocations =
                requestedAllocations != null ? requestedAllocations : new ArrayList<>();

        if (allocations.isEmpty()) {
            List<SalesInvoice> open =
                    salesInvoiceRepository.findOpenReceivablesForCustomer(tenantId, customerId);
            boolean hasGlInvoice = open.stream().anyMatch(this::isPostedToGl);
            if (!hasGlInvoice) {
                throw new IllegalStateException(
                        "Cannot post receipt: no issued customer invoices posted to GL. "
                                + "Approve a sales invoice first (Dr AR / Cr Revenue).");
            }
            return;
        }

        for (ReceiptAllocationDto dto : allocations) {
            if (dto.getSalesInvoiceId() == null) {
                continue;
            }
            SalesInvoice invoice = salesInvoiceRepository
                    .findByTenantIdAndId(tenantId, dto.getSalesInvoiceId())
                    .orElseThrow(() -> new IllegalArgumentException("Sales invoice not found"));
            assertInvoicePostedToGl(invoice);
            BigDecimal outstanding = subledgerBalanceService.computeOutstanding(tenantId, invoice, receipt.getId());
            if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("Invoice " + invoice.getInvoiceNumber() + " has no outstanding balance");
            }
        }
    }

    private boolean isPostedToGl(SalesInvoice invoice) {
        return invoice.getGlJournalId() != null
                && (invoice.getStatus() == SalesInvoice.SalesInvoiceStatus.ISSUED
                        || invoice.getStatus() == SalesInvoice.SalesInvoiceStatus.PARTIALLY_PAID);
    }

    private static void assertInvoicePostedToGl(SalesInvoice invoice) {
        if (invoice.getGlJournalId() == null) {
            throw new IllegalStateException(
                    "Invoice " + invoice.getInvoiceNumber() + " has no GL accrual. Approve/issue the invoice first.");
        }
        if (invoice.getStatus() == SalesInvoice.SalesInvoiceStatus.DRAFT) {
            throw new IllegalStateException("Invoice " + invoice.getInvoiceNumber() + " is still draft.");
        }
    }
}
