package com.finance.transactional.service;

import com.finance.transactional.dto.ReceiptAllocationDto;
import com.finance.transactional.model.ar.Receipt;
import com.finance.transactional.model.ar.ReceiptAllocation;
import com.finance.transactional.model.ar.SalesInvoice;
import com.finance.transactional.repository.ReceiptAllocationRepository;
import com.finance.transactional.repository.SalesInvoiceRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArReceiptApplicationService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final ReceiptAllocationRepository receiptAllocationRepository;
    private final ArSubledgerBalanceService subledgerBalanceService;

    /**
     * Applies receipt to invoices. If no allocations provided, applies to oldest open invoices first (on-account).
     */
    public List<ReceiptAllocation> applyReceipt(Receipt receipt, List<ReceiptAllocationDto> requestedAllocations) {
        UUID tenantId = receipt.getTenantId();
        UUID customerId = receipt.getCustomer().getId();
        BigDecimal receiptAmount = receipt.getAmount() != null ? receipt.getAmount() : BigDecimal.ZERO;

        if (receiptAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Receipt amount must be greater than zero");
        }

        List<ReceiptAllocationDto> toApply = requestedAllocations != null ? new ArrayList<>(requestedAllocations) : new ArrayList<>();

        if (toApply.isEmpty()) {
            toApply = buildOldestFirstAllocations(tenantId, customerId, receipt.getId(), receiptAmount);
        }

        BigDecimal totalApplied = BigDecimal.ZERO;
        List<ReceiptAllocation> saved = new ArrayList<>();

        for (ReceiptAllocationDto dto : toApply) {
            if (dto.getSalesInvoiceId() == null || dto.getAllocatedAmount() == null) {
                continue;
            }
            BigDecimal allocAmt = dto.getAllocatedAmount();
            if (allocAmt.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            SalesInvoice invoice = salesInvoiceRepository
                    .findByTenantIdAndId(tenantId, dto.getSalesInvoiceId())
                    .orElseThrow(() -> new IllegalArgumentException("Sales invoice not found: " + dto.getSalesInvoiceId()));

            if (!invoice.getCustomer().getId().equals(customerId)) {
                throw new IllegalArgumentException("Invoice " + invoice.getInvoiceNumber() + " does not belong to this customer");
            }

            BigDecimal outstanding =
                    subledgerBalanceService.computeOutstanding(tenantId, invoice, receipt.getId());
            if (allocAmt.compareTo(outstanding) > 0) {
                throw new IllegalArgumentException(
                        "Allocation "
                                + allocAmt
                                + " exceeds outstanding "
                                + outstanding
                                + " on invoice "
                                + invoice.getInvoiceNumber());
            }

            ReceiptAllocation allocation = new ReceiptAllocation();
            allocation.setTenantId(tenantId);
            allocation.setReceipt(receipt);
            allocation.setSalesInvoice(invoice);
            allocation.setAllocatedAmount(allocAmt);
            saved.add(allocation);
            totalApplied = totalApplied.add(allocAmt);
            refreshInvoicePaymentStatus(tenantId, invoice);
        }

        if (totalApplied.compareTo(receiptAmount) > 0) {
            throw new IllegalArgumentException(
                    "Total applied " + totalApplied + " exceeds receipt amount " + receiptAmount);
        }

        receipt.getAllocations().clear();
        receipt.getAllocations().addAll(saved);
        return saved;
    }

    private List<ReceiptAllocationDto> buildOldestFirstAllocations(
            UUID tenantId, UUID customerId, UUID receiptId, BigDecimal receiptAmount) {
        List<SalesInvoice> openInvoices =
                salesInvoiceRepository.findOpenReceivablesForCustomer(tenantId, customerId);
        openInvoices.sort(Comparator.comparing(SalesInvoice::getInvoiceDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SalesInvoice::getInvoiceNumber, Comparator.nullsLast(Comparator.naturalOrder())));

        List<ReceiptAllocationDto> result = new ArrayList<>();
        BigDecimal remaining = receiptAmount;

        for (SalesInvoice invoice : openInvoices) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal outstanding = subledgerBalanceService.computeOutstanding(tenantId, invoice, receiptId);
            if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal apply = remaining.min(outstanding);
            ReceiptAllocationDto dto = new ReceiptAllocationDto();
            dto.setSalesInvoiceId(invoice.getId());
            dto.setAllocatedAmount(apply);
            result.add(dto);
            remaining = remaining.subtract(apply);
        }
        return result;
    }

    private void refreshInvoicePaymentStatus(UUID tenantId, SalesInvoice invoice) {
        BigDecimal outstanding = subledgerBalanceService.computeOutstanding(tenantId, invoice, null);
        if (outstanding.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(SalesInvoice.SalesInvoiceStatus.PAID);
        } else {
            BigDecimal allocated = subledgerBalanceService.computeAllocated(tenantId, invoice.getId(), null);
            if (allocated.compareTo(BigDecimal.ZERO) > 0) {
                invoice.setStatus(SalesInvoice.SalesInvoiceStatus.PARTIALLY_PAID);
            }
        }
        salesInvoiceRepository.save(invoice);
    }
}
