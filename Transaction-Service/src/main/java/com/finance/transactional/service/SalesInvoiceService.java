package com.finance.transactional.service;

import com.finance.transactional.client.NumberingSeriesClient;
import com.finance.transactional.dto.ArInterestRequest;
import com.finance.transactional.dto.ArWriteOffRequest;
import com.finance.transactional.dto.SalesInvoiceDto;
import com.finance.transactional.dto.event.ArSalesInvoiceGlPostResult;
import com.finance.transactional.exception.ApInvoiceApprovalException;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.mapper.SalesInvoiceMapper;
import com.finance.transactional.model.ar.SalesInvoice;
import com.finance.transactional.repository.SalesInvoiceRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.finance.transactional.event.DomainEventPublisher;
import java.util.HashMap;
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
public class SalesInvoiceService {

    private final SalesInvoiceRepository repository;
    private final SalesInvoiceMapper mapper;
    private final NumberingSeriesClient numberingSeriesClient;
    private final ArSubledgerBalanceService subledgerBalanceService;
    private final ArGlPostingGateway arGlPostingGateway;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public SalesInvoiceDto createSalesInvoice(UUID tenantId, SalesInvoiceDto dto) {
        SalesInvoice salesInvoice = mapper.toEntity(dto);
        salesInvoice.setTenantId(tenantId);
        if (salesInvoice.getStatus() == null) {
            salesInvoice.setStatus(SalesInvoice.SalesInvoiceStatus.DRAFT);
        }

        if (salesInvoice.getInvoiceNumber() == null || salesInvoice.getInvoiceNumber().isBlank()) {
            try {
                Map<String, String> result = numberingSeriesClient.getNextNumber("AR_INVOICE");
                salesInvoice.setInvoiceNumber(result.get("nextNumber"));
            } catch (Exception e) {
                log.warn("Failed to fetch next AR invoice number: {}", e.getMessage());
            }
        }

        SalesInvoice saved = repository.save(salesInvoice);
        return enrichDto(saved);
    }

    @Transactional
    public SalesInvoiceDto updateSalesInvoice(UUID tenantId, UUID id, SalesInvoiceDto dto) {
        SalesInvoice existing = getExistingSalesInvoice(tenantId, id);
        if (existing.getStatus() != SalesInvoice.SalesInvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT sales invoices can be edited");
        }
        SalesInvoice updated = mapper.toEntity(dto);

        existing.setInvoiceNumber(updated.getInvoiceNumber());
        if (updated.getCustomer() != null && updated.getCustomer().getId() != null) {
            existing.setCustomer(updated.getCustomer());
        }
        existing.setInvoiceDate(updated.getInvoiceDate());
        existing.setDueDate(updated.getDueDate());
        existing.setTotalAmount(updated.getTotalAmount());
        existing.setTaxAmount(updated.getTaxAmount());
        existing.setCurrency(updated.getCurrency());
        if (updated.getIsMatched() != null) {
            existing.setIsMatched(updated.getIsMatched());
        }

        return enrichDto(repository.save(existing));
    }

    @Transactional(readOnly = true)
    public SalesInvoiceDto getSalesInvoiceById(UUID tenantId, UUID id) {
        return enrichDto(getExistingSalesInvoice(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<SalesInvoiceDto> getAllSalesInvoices(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream().map(this::enrichDto).toList();
    }

    @Transactional
    public SalesInvoiceDto approveSalesInvoice(UUID tenantId, UUID id) {
        SalesInvoice invoice = getExistingSalesInvoice(tenantId, id);
        if (invoice.getStatus() != SalesInvoice.SalesInvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT sales invoices can be approved/issued.");
        }
        if (invoice.getTotalAmount() == null || invoice.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invoice amount must be greater than zero");
        }
        if (invoice.getGlJournalId() != null) {
            throw new IllegalStateException(
                    "Invoice already posted to GL (journal " + invoice.getGlJournalNumber() + ")");
        }

        ArSalesInvoiceGlPostResult glResult;
        try {
            glResult = arGlPostingGateway.postSalesInvoiceAccrualAndWait(invoice);
        } catch (Exception ex) {
            log.error("GL posting failed for AR invoice {}", invoice.getInvoiceNumber(), ex);
            throw new ApInvoiceApprovalException(
                    "Sales invoice approval failed: could not post to General Ledger. " + ex.getMessage(), ex);
        }
        if (glResult == null || !glResult.isSuccess()) {
            String message = glResult != null && glResult.getMessage() != null
                    ? glResult.getMessage()
                    : "General Ledger posting failed";
            throw new ApInvoiceApprovalException("Sales invoice approval failed: " + message);
        }

        invoice.setStatus(SalesInvoice.SalesInvoiceStatus.ISSUED);
        invoice.setGlJournalId(glResult.getJournalId());
        invoice.setGlJournalNumber(glResult.getJournalNumber());
        return enrichDto(repository.save(invoice));
    }

    @Transactional
    public SalesInvoiceDto writeOff(UUID tenantId, UUID id, ArWriteOffRequest request) {
        SalesInvoice invoice = getExistingSalesInvoice(tenantId, id);
        validateReceivableAdjustment(invoice, "Write-off");

        BigDecimal outstanding = subledgerBalanceService.computeBalance(tenantId, invoice, null);
        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "Invoice " + invoice.getInvoiceNumber() + " has no outstanding balance to write off"
                            + " (status: " + invoice.getStatus() + ").");
        }
        if (request.getAmount().compareTo(outstanding) > 0) {
            throw new IllegalArgumentException("Write-off amount exceeds outstanding balance " + outstanding);
        }

        LocalDate adjDate = request.getAdjustmentDate() != null ? request.getAdjustmentDate() : LocalDate.now();
        Map<String, Object> event = new HashMap<>();
        event.put("tenantId", tenantId);
        event.put("invoiceId", invoice.getId());
        event.put("invoiceNumber", invoice.getInvoiceNumber());
        event.put("adjustmentDate", adjDate);
        event.put("amount", request.getAmount());
        event.put("reason", request.getReason());
        domainEventPublisher.publish("ar-write-off", event);

        BigDecimal newOutstanding = outstanding.subtract(request.getAmount());
        if (newOutstanding.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(SalesInvoice.SalesInvoiceStatus.PAID);
        } else if (invoice.getStatus() == SalesInvoice.SalesInvoiceStatus.ISSUED) {
            invoice.setStatus(SalesInvoice.SalesInvoiceStatus.PARTIALLY_PAID);
        }
        return enrichDto(repository.save(invoice));
    }

    @Transactional
    public SalesInvoiceDto applyInterest(UUID tenantId, UUID id, ArInterestRequest request) {
        SalesInvoice invoice = getExistingSalesInvoice(tenantId, id);
        validateReceivableAdjustment(invoice, "Interest");

        LocalDate assessmentDate =
                request.getAssessmentDate() != null ? request.getAssessmentDate() : LocalDate.now();
        Map<String, Object> event = new HashMap<>();
        event.put("tenantId", tenantId);
        event.put("invoiceId", invoice.getId());
        event.put("invoiceNumber", invoice.getInvoiceNumber());
        event.put("assessmentDate", assessmentDate);
        event.put("interestAmount", request.getInterestAmount());
        event.put("reason", request.getReason());
        domainEventPublisher.publish("ar-interest", event);

        invoice.setTotalAmount(invoice.getTotalAmount().add(request.getInterestAmount()));
        return enrichDto(repository.save(invoice));
    }

    @Transactional
    public void deleteSalesInvoice(UUID tenantId, UUID id) {
        SalesInvoice salesInvoice = getExistingSalesInvoice(tenantId, id);
        if (salesInvoice.getStatus() != SalesInvoice.SalesInvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT sales invoices can be deleted");
        }
        repository.delete(salesInvoice);
    }

    private void validateReceivableAdjustment(SalesInvoice invoice, String operation) {
        if (invoice.getStatus() == SalesInvoice.SalesInvoiceStatus.DRAFT) {
            throw new IllegalStateException(
                    operation
                            + " requires an approved invoice. Current status: DRAFT — use Approve in the Approval Workbench first.");
        }
        if (invoice.getStatus() == SalesInvoice.SalesInvoiceStatus.CANCELLED) {
            throw new IllegalStateException(operation + " cannot be applied to a cancelled invoice.");
        }
        if (invoice.getGlJournalId() == null) {
            throw new IllegalStateException(
                    "Invoice " + invoice.getInvoiceNumber() + " is not posted to GL. Approve the invoice first.");
        }
    }

    private SalesInvoiceDto enrichDto(SalesInvoice invoice) {
        SalesInvoiceDto dto = mapper.toDto(invoice);
        if (invoice.getCustomer() != null) {
            dto.setCustomerName(invoice.getCustomer().getCustomerName());
        }
        BigDecimal paid = subledgerBalanceService.computeAllocated(tenantId(invoice), invoice.getId(), null);
        dto.setPaidAmount(paid);
        dto.setOutstandingAmount(subledgerBalanceService.computeOutstanding(tenantId(invoice), invoice, null));
        return dto;
    }

    private UUID tenantId(SalesInvoice invoice) {
        return invoice.getTenantId();
    }

    private SalesInvoice getExistingSalesInvoice(UUID tenantId, UUID id) {
        return repository
                .findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesInvoice not found with id " + id));
    }
}
