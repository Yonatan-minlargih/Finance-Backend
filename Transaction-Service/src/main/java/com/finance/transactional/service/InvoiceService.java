package com.finance.transactional.service;

import com.finance.transactional.client.CoreFinanceJournalClient;
import com.finance.transactional.config.SecurityUtil;
import com.finance.transactional.dto.InvoiceAuditTrailDto;
import com.finance.transactional.dto.InvoiceDto;
import com.finance.transactional.dto.corefinance.CoreFinanceJournalReverseResponse;
import com.finance.transactional.dto.event.ApInvoiceGlPostResult;
import com.finance.transactional.client.NumberingSeriesClient;
import com.finance.transactional.model.ap.Invoice;
import com.finance.transactional.model.ap.InvoiceLine;
import com.finance.transactional.exception.ApInvoiceApprovalException;
import com.finance.transactional.exception.DuplicateInvoiceException;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.mapper.InvoiceMapper;
import com.finance.transactional.model.ap.Vendor;
import com.finance.transactional.repository.InvoiceRepository;
import com.finance.transactional.repository.PaymentAllocationRepository;
import com.finance.transactional.repository.VendorRepository;
import feign.FeignException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceGlPostingGateway invoiceGlPostingGateway;
    private final NumberingSeriesClient numberingSeriesClient;
    private final InvoiceDistributionValidator distributionValidator;
    private final InvoiceAccountingPeriodService invoiceAccountingPeriodService;
    private final PurchaseOrderLinkageService purchaseOrderLinkageService;
    private final ApCurrencyService apCurrencyService;
    private final VendorRepository vendorRepository;
    private final CoreFinanceJournalClient coreFinanceJournalClient;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final SecurityUtil securityUtil;

    private static final BigDecimal DEFAULT_VAT_RATE = new BigDecimal("15");

    
    @Transactional
    public InvoiceDto createInvoice(UUID tenantId, InvoiceDto invoiceDto) {
        Invoice invoice = invoiceMapper.toEntity(invoiceDto);
        invoice.setTenantId(tenantId);
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);

        assertUniqueInvoiceNumber(tenantId, invoice.getVendor().getId(), invoice.getInvoiceNumber(), null);
        applyVendorTaxSnapshot(invoice, tenantId);
        applyDefaultVatRate(invoice);

        // Auto-generate invoice number from Core-Finance numbering series (SRS FR_1.4)
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
            try {
                Map<String, String> result = numberingSeriesClient.getNextNumber("AP_INVOICE");
                invoice.setInvoiceNumber(result.get("nextNumber"));
                log.info("Auto-assigned AP invoice number: {}", invoice.getInvoiceNumber());
            } catch (Exception e) {
                log.warn("Failed to fetch next AP invoice number from Core-Finance, will use repository default: {}", e.getMessage());
            }
        }

        attachLines(invoice, tenantId);
        distributionValidator.validateAndNormalize(invoice);
        apCurrencyService.applyVendorCurrencyToInvoice(
                invoice,
                tenantId,
                invoiceDto.getCurrency(),
                invoiceDto.getExchangeRate(),
                invoiceDto.getForeignTotalAmount() != null ? invoiceDto.getForeignTotalAmount() : invoiceDto.getTotalAmount());
        purchaseOrderLinkageService.applyInvoicePurchaseOrder(invoice, tenantId, null);
        invoiceAccountingPeriodService.resolveAndValidateOpenPeriod(invoice);
        Invoice saved = invoiceRepository.save(invoice);
        return invoiceMapper.toDto(saved);
    }

    
    @Transactional
    public InvoiceDto approveInvoice(UUID tenantId, UUID id) {
        Invoice invoice = getExistingInvoice(tenantId, id);

        if (invoice.getStatus() != Invoice.InvoiceStatus.DRAFT
                && invoice.getStatus() != Invoice.InvoiceStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Invoice must be in DRAFT or PENDING_APPROVAL status to be approved");
        }

        if (invoice.getGlJournalId() != null) {
            throw new IllegalStateException(
                    "Invoice is already posted to the General Ledger (journal "
                            + invoice.getGlJournalNumber() + ")");
        }

        if (invoice.getVendor() != null) {
            invoice.getVendor().getId();
        }
        if (invoice.getLines() != null) {
            invoice.getLines().size();
        }
        distributionValidator.validateAndNormalize(invoice);
        invoiceAccountingPeriodService.resolveAndValidateOpenPeriod(invoice);

        ApInvoiceGlPostResult glResult;
        try {
            glResult = invoiceGlPostingGateway.postInvoiceAccrualAndWait(invoice);
        } catch (Exception ex) {
            log.error("GL posting failed for invoice {}", invoice.getInvoiceNumber(), ex);
            throw new ApInvoiceApprovalException(
                    "Invoice approval failed: could not post accrual to General Ledger. " + ex.getMessage(),
                    ex);
        }

        if (glResult == null || !glResult.isSuccess()) {
            String message = glResult != null && glResult.getMessage() != null
                    ? glResult.getMessage()
                    : "General Ledger posting failed";
            throw new ApInvoiceApprovalException("Invoice approval failed: " + message);
        }

        LocalDateTime now = LocalDateTime.now();
        String actor = securityUtil.getActorDisplayName();
        invoice.setApprovedBy(actor);
        invoice.setApprovedAt(now);
        invoice.setPostedBy(actor);
        invoice.setPostedAt(now);
        invoice.setStatus(Invoice.InvoiceStatus.POSTED);
        invoice.setGlJournalId(glResult.getJournalId());
        invoice.setGlJournalNumber(glResult.getJournalNumber());
        if (glResult.getAccountingPeriodId() != null) {
            invoice.setGlAccountingPeriodId(glResult.getAccountingPeriodId());
        }
        if (glResult.getFiscalYearId() != null) {
            invoice.setGlFiscalYearId(glResult.getFiscalYearId());
        }
        Invoice updated = invoiceRepository.save(invoice);

        log.info("AP invoice {} approved and posted to GL as journal {}",
                updated.getInvoiceNumber(), updated.getGlJournalNumber());

        return invoiceMapper.toDto(updated);
    }

    
    @Transactional(readOnly = true)
    public InvoiceDto getInvoiceById(UUID tenantId, UUID id) {
        return invoiceMapper.toDto(getExistingInvoice(tenantId, id));
    }

    @Transactional(readOnly = true)
    public InvoiceAuditTrailDto getInvoiceAuditTrail(UUID tenantId, UUID id) {
        return buildAuditTrail(getExistingInvoice(tenantId, id));
    }

    @Transactional
    public InvoiceDto voidInvoice(UUID tenantId, UUID id, String reversalReason) {
        Invoice invoice = getExistingInvoice(tenantId, id);

        if (invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Invoice is already voided");
        }
        if (invoice.getGlJournalId() == null) {
            throw new IllegalStateException(
                    "Only posted invoices with a GL journal can be voided. Approve the invoice first or delete draft invoices.");
        }
        if (invoice.getGlReversalJournalId() != null) {
            throw new IllegalStateException(
                    "Invoice accrual was already reversed (voucher "
                            + invoice.getGlReversalJournalNumber() + ")");
        }
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID
                || invoice.getStatus() == Invoice.InvoiceStatus.PARTIALLY_PAID) {
            throw new IllegalStateException("Cannot void an invoice that has payments applied");
        }
        if (paymentAllocationRepository.existsByTenantIdAndInvoiceId(tenantId, id)) {
            throw new IllegalStateException("Cannot void invoice with payment allocations");
        }

        String reason = reversalReason != null ? reversalReason.trim() : "";
        if (reason.isEmpty()) {
            throw new IllegalArgumentException("Reversal reason is required");
        }

        invoiceAccountingPeriodService.validateOpenPeriodForDate(tenantId, LocalDate.now());

        CoreFinanceJournalReverseResponse reversal;
        try {
            reversal = coreFinanceJournalClient.reverseJournal(invoice.getGlJournalId(), reason);
        } catch (FeignException ex) {
            log.error("GL reversal failed for invoice {}", invoice.getInvoiceNumber(), ex);
            throw new IllegalStateException(
                    "Failed to reverse General Ledger accrual: "
                            + (ex.contentUTF8() != null && !ex.contentUTF8().isBlank()
                                    ? ex.contentUTF8()
                                    : ex.getMessage()),
                    ex);
        }

        if (reversal == null || !Boolean.TRUE.equals(reversal.getSuccess()) || reversal.getJournalId() == null) {
            String message = reversal != null && reversal.getMessage() != null
                    ? reversal.getMessage()
                    : "General Ledger reversal failed";
            throw new IllegalStateException(message);
        }

        String actor = securityUtil.getActorDisplayName();
        invoice.setVoidedBy(actor);
        invoice.setVoidedAt(LocalDateTime.now());
        invoice.setVoidReason(reason);
        invoice.setGlReversalJournalId(reversal.getJournalId());
        invoice.setGlReversalJournalNumber(reversal.getJournalNumber());
        invoice.setStatus(Invoice.InvoiceStatus.CANCELLED);

        Invoice saved = invoiceRepository.save(invoice);
        log.info(
                "AP invoice {} voided; reversal journal {}",
                saved.getInvoiceNumber(),
                saved.getGlReversalJournalNumber());
        return invoiceMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<InvoiceDto> getAllInvoices(UUID tenantId) {
        return invoiceRepository.findByTenantId(tenantId).stream()
                .map(invoiceMapper::toDto)
                .toList();
    }

    @Transactional
    public InvoiceDto updateInvoice(UUID tenantId, UUID id, InvoiceDto dto) {
        Invoice existing = getExistingInvoice(tenantId, id);
        Invoice updated = invoiceMapper.toEntity(dto);

        assertUniqueInvoiceNumber(
                tenantId,
                updated.getVendor().getId(),
                updated.getInvoiceNumber(),
                existing.getId());

        existing.setInvoiceNumber(updated.getInvoiceNumber());
        existing.setVendor(updated.getVendor());
        applyVendorTaxSnapshot(existing, tenantId);
        existing.setPurchaseOrder(updated.getPurchaseOrder());
        existing.setInvoiceDate(updated.getInvoiceDate());
        existing.setDueDate(updated.getDueDate());
        existing.setTotalAmount(updated.getTotalAmount());
        existing.setTaxAmount(updated.getTaxAmount());
        existing.setSubtotalAmount(updated.getSubtotalAmount());
        existing.setVatRate(updated.getVatRate() != null ? updated.getVatRate() : existing.getVatRate());
        applyDefaultVatRate(existing);
        existing.setCurrency(updated.getCurrency());
        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus());
        }
        if (updated.getInvoiceType() != null) {
            existing.setInvoiceType(updated.getInvoiceType());
        }

        if (updated.getLines() != null) {
            existing.getLines().clear();
            for (InvoiceLine line : updated.getLines()) {
                line.setInvoice(existing);
                line.setTenantId(tenantId);
                existing.getLines().add(line);
            }
            distributionValidator.validateAndNormalize(existing);
        }

        apCurrencyService.applyVendorCurrencyToInvoice(
                existing,
                tenantId,
                dto.getCurrency() != null ? dto.getCurrency() : existing.getCurrency(),
                dto.getExchangeRate() != null ? dto.getExchangeRate() : existing.getExchangeRate(),
                dto.getForeignTotalAmount() != null ? dto.getForeignTotalAmount() : dto.getTotalAmount());
        purchaseOrderLinkageService.applyInvoicePurchaseOrder(existing, tenantId, existing.getId());
        invoiceAccountingPeriodService.resolveAndValidateOpenPeriod(existing);
        Invoice saved = invoiceRepository.save(existing);
        return invoiceMapper.toDto(saved);
    }

    @Transactional
    public void deleteInvoice(UUID tenantId, UUID id) {
        Invoice invoice = getExistingInvoice(tenantId, id);
        if (invoice.getGlJournalId() != null) {
            throw new IllegalStateException(
                    "Posted invoices cannot be deleted. Void the invoice to reverse the GL accrual.");
        }
        invoiceRepository.delete(invoice);
    }

    private Invoice getExistingInvoice(UUID tenantId, UUID id) {
        return invoiceRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id " + id));
    }

    private void attachLines(Invoice invoice, UUID tenantId) {
        if (invoice.getLines() == null) {
            return;
        }
        invoice.getLines().forEach(line -> {
            line.setInvoice(invoice);
            line.setTenantId(tenantId);
        });
    }

    private void assertUniqueInvoiceNumber(UUID tenantId, UUID vendorId, String invoiceNumber, UUID excludeId) {
        if (invoiceNumber == null || invoiceNumber.isBlank() || vendorId == null) {
            return;
        }
        boolean duplicate = excludeId == null
                ? invoiceRepository.existsByTenantIdAndVendorIdAndInvoiceNumberIgnoreCase(
                        tenantId, vendorId, invoiceNumber.trim())
                : invoiceRepository.existsByTenantIdAndVendorIdAndInvoiceNumberIgnoreCaseAndIdNot(
                        tenantId, vendorId, invoiceNumber.trim(), excludeId);
        if (duplicate) {
            Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
            String vendorName = vendor != null ? vendor.getVendorName() : vendorId.toString();
            throw new DuplicateInvoiceException(invoiceNumber.trim(), vendorName);
        }
    }

    private void applyVendorTaxSnapshot(Invoice invoice, UUID tenantId) {
        if (invoice.getVendor() == null || invoice.getVendor().getId() == null) {
            return;
        }
        vendorRepository
                .findByTenantIdAndId(tenantId, invoice.getVendor().getId())
                .ifPresent(vendor -> {
                    invoice.setVendorTaxId(vendor.getTaxId());
                    invoice.setVendorVatNumber(vendor.getVatNumber());
                });
    }

    private void applyDefaultVatRate(Invoice invoice) {
        if (invoice.getVatRate() == null) {
            invoice.setVatRate(DEFAULT_VAT_RATE);
        }
    }

    private InvoiceAuditTrailDto buildAuditTrail(Invoice invoice) {
        return InvoiceAuditTrailDto.builder()
                .invoiceId(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .status(invoice.getStatus() != null ? invoice.getStatus().name() : null)
                .created(auditStep(invoice.getCreatedBy(), invoice.getCreatedAt()))
                .approved(auditStep(invoice.getApprovedBy(), invoice.getApprovedAt()))
                .posted(auditStep(invoice.getPostedBy(), invoice.getPostedAt()))
                .voided(auditStep(invoice.getVoidedBy(), invoice.getVoidedAt()))
                .glJournal(glRef(invoice.getGlJournalId(), invoice.getGlJournalNumber()))
                .glReversalJournal(glRef(invoice.getGlReversalJournalId(), invoice.getGlReversalJournalNumber()))
                .build();
    }

    private static InvoiceAuditTrailDto.AuditStep auditStep(String by, LocalDateTime at) {
        if ((by == null || by.isBlank()) && at == null) {
            return null;
        }
        return InvoiceAuditTrailDto.AuditStep.builder().by(by).at(at).build();
    }

    private static InvoiceAuditTrailDto.GlJournalRef glRef(UUID id, String number) {
        if (id == null && (number == null || number.isBlank())) {
            return null;
        }
        return InvoiceAuditTrailDto.GlJournalRef.builder().id(id).number(number).build();
    }
}
