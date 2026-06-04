package com.finance.transactional.service;

import com.finance.transactional.dto.PurchaseOrderDto;
import com.finance.transactional.exception.PurchaseOrderInvoicedAmountExceededException;
import com.finance.transactional.exception.PurchaseOrderPaymentExceededException;
import com.finance.transactional.exception.PurchaseOrderVendorMismatchException;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.ap.Invoice;
import com.finance.transactional.model.ap.Payment;
import com.finance.transactional.model.ap.PurchaseOrder;
import com.finance.transactional.repository.InvoiceRepository;
import com.finance.transactional.repository.PaymentRepository;
import com.finance.transactional.repository.PurchaseOrderRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PurchaseOrderLinkageService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public void applyInvoicePurchaseOrder(Invoice invoice, UUID tenantId, UUID excludeInvoiceId) {
        UUID poId = resolvePurchaseOrderId(invoice);
        if (poId == null) {
            invoice.setPurchaseOrder(null);
            return;
        }

        PurchaseOrder po = purchaseOrderRepository
                .findByTenantIdAndId(tenantId, poId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found: " + poId));

        assertVendorMatchesPurchaseOrder(invoice.getVendor() != null ? invoice.getVendor().getId() : null, po);
        assertInvoiceAmountWithinPo(invoice, tenantId, po, excludeInvoiceId);
        invoice.setPurchaseOrder(po);
    }



    public void enrichDto(PurchaseOrderDto dto, UUID tenantId) {
        if (dto == null || dto.getId() == null) {
            return;
        }
        BigDecimal poTotal = nullSafe(dto.getTotalAmount());
        BigDecimal invoiced = nullSafe(sumInvoicedForPo(tenantId, dto.getId()));
        BigDecimal paid = nullSafe(sumPaidForPo(tenantId, dto.getId()));
        dto.setInvoicedAmount(invoiced);
        dto.setPaidAmount(paid);
        dto.setRemainingBalance(poTotal.subtract(invoiced).max(ZERO));
        dto.setRemainingPaymentBalance(poTotal.subtract(paid).max(ZERO));
    }

    public BigDecimal sumInvoicedForPo(UUID tenantId, UUID poId) {
        BigDecimal sum = invoiceRepository.sumTotalAmountByPurchaseOrder(tenantId, poId, null);
        return sum != null ? sum : ZERO;
    }

    public BigDecimal sumPaidForPo(UUID tenantId, UUID poId) {
        BigDecimal sum = paymentRepository.sumAmountByPurchaseOrder(tenantId, poId, null);
        return sum != null ? sum : ZERO;
    }

    private void assertVendorMatchesPurchaseOrder(UUID vendorId, PurchaseOrder po) {
        if (vendorId == null || po.getVendor() == null || po.getVendor().getId() == null) {
            throw new IllegalArgumentException("Vendor is required when linking to a purchase order");
        }
        if (!vendorId.equals(po.getVendor().getId())) {
            throw new PurchaseOrderVendorMismatchException();
        }
    }

    private void assertInvoiceAmountWithinPo(
            Invoice invoice, UUID tenantId, PurchaseOrder po, UUID excludeInvoiceId) {
        BigDecimal poTotal = nullSafe(po.getTotalAmount());
        BigDecimal invoiceTotal = nullSafe(invoice.getTotalAmount());
        BigDecimal alreadyInvoiced =
                nullSafe(invoiceRepository.sumTotalAmountByPurchaseOrder(tenantId, po.getId(), excludeInvoiceId));
        BigDecimal newTotal = alreadyInvoiced.add(invoiceTotal);
        if (newTotal.compareTo(poTotal) > 0) {
            throw new PurchaseOrderInvoicedAmountExceededException(
                    PurchaseOrderInvoicedAmountExceededException.MESSAGE
                            + " (PO "
                            + po.getPoNumber()
                            + ": limit "
                            + poTotal
                            + ", already invoiced "
                            + alreadyInvoiced
                            + ", this invoice "
                            + invoiceTotal
                            + ")");
        }
    }



    private UUID resolvePurchaseOrderId(Invoice invoice) {
        if (invoice.getPurchaseOrder() == null) {
            return null;
        }
        return invoice.getPurchaseOrder().getId();
    }

    private static BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : ZERO;
    }
}
