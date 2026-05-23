package com.finance.transactional.service;

import com.finance.transactional.dto.SalesInvoiceDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.ar.SalesInvoice;
import com.finance.transactional.event.DomainEventPublisher;
import com.finance.transactional.mapper.SalesInvoiceMapper;
import com.finance.transactional.repository.SalesInvoiceRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SalesInvoiceService {

    private final SalesInvoiceRepository repository;
    private final SalesInvoiceMapper mapper;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public SalesInvoiceDto createSalesInvoice(UUID tenantId, SalesInvoiceDto dto) {
        SalesInvoice salesInvoice = mapper.toEntity(dto);
        salesInvoice.setTenantId(tenantId);
        SalesInvoice saved = repository.save(salesInvoice);
        SalesInvoiceDto resultDto = mapper.toDto(saved);

        // Publish event
        domainEventPublisher.publish("sales-invoice-created", resultDto);

        return resultDto;
    }

    @Transactional
    public SalesInvoiceDto updateSalesInvoice(UUID tenantId, UUID id, SalesInvoiceDto dto) {
        SalesInvoice existing = getExistingSalesInvoice(tenantId, id);
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
        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus());
        }
        if (updated.getIsMatched() != null) {
            existing.setIsMatched(updated.getIsMatched());
        }

        SalesInvoice saved = repository.save(existing);
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public SalesInvoiceDto getSalesInvoiceById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingSalesInvoice(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<SalesInvoiceDto> getAllSalesInvoices(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public SalesInvoiceDto approveSalesInvoice(UUID tenantId, UUID id) {
        SalesInvoice invoice = getExistingSalesInvoice(tenantId, id);
        if (invoice.getStatus() != SalesInvoice.SalesInvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT sales invoices can be approved/issued.");
        }
        invoice.setStatus(SalesInvoice.SalesInvoiceStatus.ISSUED);
        SalesInvoice saved = repository.save(invoice);
        SalesInvoiceDto resultDto = mapper.toDto(saved);

        // Publish event for General Ledger integration
        domainEventPublisher.publish("sales-invoice-approved", resultDto);

        return resultDto;
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, java.math.BigDecimal> getAgingAnalysis(UUID tenantId, java.time.LocalDate asOfDate) {
        List<SalesInvoice> outstandingInvoices = repository.findByTenantId(tenantId).stream()
                .filter(i -> i.getStatus() == SalesInvoice.SalesInvoiceStatus.ISSUED || i.getStatus() == SalesInvoice.SalesInvoiceStatus.PARTIALLY_PAID)
                .toList();

        java.math.BigDecimal current = java.math.BigDecimal.ZERO;
        java.math.BigDecimal days30 = java.math.BigDecimal.ZERO;
        java.math.BigDecimal days60 = java.math.BigDecimal.ZERO;
        java.math.BigDecimal days90 = java.math.BigDecimal.ZERO;
        java.math.BigDecimal days120 = java.math.BigDecimal.ZERO;
        java.math.BigDecimal over120 = java.math.BigDecimal.ZERO;

        for (SalesInvoice inv : outstandingInvoices) {
            // Determine outstanding amount. If receipt allocations exist, we should subtract them, but for now we'll just take totalAmount if not fully paid.
            // A more complex system would sum ReceiptAllocations. For now, we assume totalAmount is outstanding if ISSUED.
            java.math.BigDecimal outstanding = inv.getTotalAmount();
            
            long daysOld = java.time.temporal.ChronoUnit.DAYS.between(inv.getInvoiceDate(), asOfDate);
            if (daysOld <= 0) current = current.add(outstanding);
            else if (daysOld <= 30) days30 = days30.add(outstanding);
            else if (daysOld <= 60) days60 = days60.add(outstanding);
            else if (daysOld <= 90) days90 = days90.add(outstanding);
            else if (daysOld <= 120) days120 = days120.add(outstanding);
            else over120 = over120.add(outstanding);
        }

        java.util.Map<String, java.math.BigDecimal> buckets = new java.util.LinkedHashMap<>();
        buckets.put("Current", current);
        buckets.put("1-30 Days", days30);
        buckets.put("31-60 Days", days60);
        buckets.put("61-90 Days", days90);
        buckets.put("91-120 Days", days120);
        buckets.put("Over 120 Days", over120);

        return buckets;
    }

    @Transactional
    public void deleteSalesInvoice(UUID tenantId, UUID id) {
        SalesInvoice salesInvoice = getExistingSalesInvoice(tenantId, id);
        repository.delete(salesInvoice);
    }

    private SalesInvoice getExistingSalesInvoice(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesInvoice not found with id " + id));
    }
}
