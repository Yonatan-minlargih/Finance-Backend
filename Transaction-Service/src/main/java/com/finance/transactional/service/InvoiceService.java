package com.finance.transactional.service;

import com.finance.transactional.dto.InvoiceDto;
import com.finance.transactional.client.CoreFinanceClient;
import com.finance.transactional.model.ap.Invoice;
import com.finance.transactional.model.ap.InvoiceLine;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.event.DomainEventPublisher;
import com.finance.transactional.mapper.InvoiceMapper;
import com.finance.transactional.repository.InvoiceRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final CoreFinanceClient coreFinanceClient;
    private final DomainEventPublisher domainEventPublisher;

    
    @Transactional
    public InvoiceDto createInvoice(UUID tenantId, InvoiceDto invoiceDto) {
        Invoice invoice = invoiceMapper.toEntity(invoiceDto);
        invoice.setTenantId(tenantId);
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        if (invoice.getLines() != null) {
            invoice.getLines().forEach(line -> {
                line.setInvoice(invoice);
                line.setTenantId(tenantId);
            });
        }
        Invoice saved = invoiceRepository.save(invoice);
        return invoiceMapper.toDto(saved);
    }

    
    @Transactional
    public InvoiceDto approveInvoice(UUID tenantId, UUID id) {
        Invoice invoice = getExistingInvoice(tenantId, id);

        if (invoice.getStatus() != Invoice.InvoiceStatus.DRAFT && invoice.getStatus() != Invoice.InvoiceStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Invoice must be in DRAFT or PENDING_APPROVAL status to be approved");
        }

        invoice.setStatus(Invoice.InvoiceStatus.APPROVED);
        Invoice updated = invoiceRepository.save(invoice);
        InvoiceDto resultDto = invoiceMapper.toDto(updated);

        // Integration moved to a dedicated client layer without changing behavior.
        coreFinanceClient.postInvoiceApprovalJournal(updated);

        // Publish event
        domainEventPublisher.publish("invoice-approved", resultDto);

        return resultDto;
    }

    
    public InvoiceDto getInvoiceById(UUID tenantId, UUID id) {
        return invoiceMapper.toDto(getExistingInvoice(tenantId, id));
    }

    
    public List<InvoiceDto> getAllInvoices(UUID tenantId) {
        return invoiceRepository.findByTenantId(tenantId).stream()
                .map(invoiceMapper::toDto)
                .toList();
    }

    @Transactional
    public InvoiceDto updateInvoice(UUID tenantId, UUID id, InvoiceDto dto) {
        Invoice existing = getExistingInvoice(tenantId, id);
        Invoice updated = invoiceMapper.toEntity(dto);

        existing.setInvoiceNumber(updated.getInvoiceNumber());
        existing.setVendor(updated.getVendor());
        existing.setPurchaseOrder(updated.getPurchaseOrder());
        existing.setInvoiceDate(updated.getInvoiceDate());
        existing.setDueDate(updated.getDueDate());
        existing.setTotalAmount(updated.getTotalAmount());
        existing.setTaxAmount(updated.getTaxAmount());
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
        }

        Invoice saved = invoiceRepository.save(existing);
        return invoiceMapper.toDto(saved);
    }

    @Transactional
    public void deleteInvoice(UUID tenantId, UUID id) {
        Invoice invoice = getExistingInvoice(tenantId, id);
        invoiceRepository.delete(invoice);
    }

    private Invoice getExistingInvoice(UUID tenantId, UUID id) {
        return invoiceRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id " + id));
    }
}
