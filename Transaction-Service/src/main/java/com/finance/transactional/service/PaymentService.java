package com.finance.transactional.service;

import com.finance.transactional.dto.PaymentDto;
import com.finance.transactional.client.NumberingSeriesClient;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.ap.Payment;
import com.finance.transactional.dto.event.ApPaymentGlPostResult;
import com.finance.transactional.exception.ApInvoiceApprovalException;
import com.finance.transactional.mapper.PaymentMapper;
import com.finance.transactional.model.ap.Invoice;
import com.finance.transactional.repository.InvoiceRepository;
import com.finance.transactional.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    private final NumberingSeriesClient numberingSeriesClient;
    private final PurchaseOrderLinkageService purchaseOrderLinkageService;
    private final PaymentSupportService paymentSupportService;
    private final InvoiceRepository invoiceRepository;
    private final ApCurrencyService apCurrencyService;
    private final ApPaymentValidationService apPaymentValidationService;
    private final ApPaymentGlPostingGateway apPaymentGlPostingGateway;

    @Transactional
    public PaymentDto createPayment(UUID tenantId, PaymentDto dto) {
        Payment payment = mapper.toEntity(dto);
        payment.setTenantId(tenantId);

        paymentSupportService.preparePayment(payment, dto, tenantId, apCurrencyService);
        applyPaymentInvoice(payment, tenantId, dto.getInvoiceId(), null);

        if (payment.getPaymentNumber() == null || payment.getPaymentNumber().isBlank()) {
            try {
                Map<String, String> result = numberingSeriesClient.getNextNumber("PAYMENT");
                payment.setPaymentNumber(result.get("nextNumber"));
                log.info("Auto-assigned payment number: {}", payment.getPaymentNumber());
            } catch (Exception e) {
                log.warn("Failed to fetch next payment number from Core-Finance: {}", e.getMessage());
            }
        }

        Payment saved = repository.save(payment);
        apPaymentValidationService.validatePaymentCanPostToGl(saved);

        ApPaymentGlPostResult glResult;
        try {
            glResult = apPaymentGlPostingGateway.postPaymentAndWait(saved);
        } catch (Exception ex) {
            throw new ApInvoiceApprovalException(
                    "Payment failed: could not post to General Ledger. " + ex.getMessage(), ex);
        }
        if (glResult == null || !glResult.isSuccess()) {
            String message = glResult != null && glResult.getMessage() != null
                    ? glResult.getMessage()
                    : "General Ledger posting failed";
            throw new ApInvoiceApprovalException("Payment failed: " + message);
        }
        return mapper.toDto(saved);
    }

    @Transactional
    public PaymentDto updatePayment(UUID tenantId, UUID id, PaymentDto dto) {
        Payment existing = getExistingPayment(tenantId, id);
        Payment updated = mapper.toEntity(dto);

        existing.setPaymentNumber(updated.getPaymentNumber());
        existing.setVendor(updated.getVendor());
        existing.setPaymentDate(updated.getPaymentDate());
        existing.setPaymentMethod(updated.getPaymentMethod());
        existing.setReferenceNumber(updated.getReferenceNumber());

        paymentSupportService.preparePayment(existing, dto, tenantId, apCurrencyService);
        applyPaymentInvoice(existing, tenantId, dto.getInvoiceId(), existing.getId());

        boolean wasCleared = existing.getReferenceNumber() != null && !existing.getReferenceNumber().isBlank();
        boolean isCleared = existing.getReferenceNumber() != null && !existing.getReferenceNumber().isBlank();

        Payment saved = repository.save(existing);
        PaymentDto resultDto = mapper.toDto(saved);

        return resultDto;
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingPayment(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getAllPayments(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deletePayment(UUID tenantId, UUID id) {
        Payment payment = getExistingPayment(tenantId, id);
        repository.delete(payment);
    }

    private Payment getExistingPayment(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id " + id));
    }

    private void applyPaymentInvoice(Payment payment, UUID tenantId, UUID invoiceId, UUID excludePaymentId) {
        if (invoiceId == null) {
            payment.setInvoice(null);
            return;
        }

        Invoice invoice = invoiceRepository
                .findByTenantIdAndId(tenantId, invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));

        if (payment.getVendor() != null && invoice.getVendor() != null 
            && !payment.getVendor().getId().equals(invoice.getVendor().getId())) {
            throw new IllegalArgumentException("Payment vendor does not match Invoice vendor");
        }

        BigDecimal invoiceTotal = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal amount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        BigDecimal alreadyPaid = repository.sumAmountByInvoice(tenantId, invoice.getId(), excludePaymentId);
        if (alreadyPaid == null) alreadyPaid = BigDecimal.ZERO;

        BigDecimal newTotal = alreadyPaid.add(amount);
        if (newTotal.compareTo(invoiceTotal) > 0) {
            throw new IllegalArgumentException(
                    String.format("Payment exceeded invoice limit. (Invoice limit: %s, Already Paid: %s, Current: %s)", 
                        invoiceTotal, alreadyPaid, amount));
        }

        payment.setInvoice(invoice);
    }
}
