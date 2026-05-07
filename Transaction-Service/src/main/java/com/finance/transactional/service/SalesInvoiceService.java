package com.finance.transactional.service;

import com.finance.transactional.dto.SalesInvoiceDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.ar.SalesInvoice;
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

    @Transactional
    public SalesInvoiceDto createSalesInvoice(UUID tenantId, SalesInvoiceDto dto) {
        SalesInvoice salesInvoice = mapper.toEntity(dto);
        salesInvoice.setTenantId(tenantId);
        SalesInvoice saved = repository.save(salesInvoice);
        return mapper.toDto(saved);
    }

    @Transactional
    public SalesInvoiceDto updateSalesInvoice(UUID tenantId, UUID id, SalesInvoiceDto dto) {
        SalesInvoice existing = getExistingSalesInvoice(tenantId, id);
        SalesInvoice updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
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
    public void deleteSalesInvoice(UUID tenantId, UUID id) {
        SalesInvoice salesInvoice = getExistingSalesInvoice(tenantId, id);
        repository.delete(salesInvoice);
    }

    private SalesInvoice getExistingSalesInvoice(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesInvoice not found with id " + id));
    }
}
