package com.finance.transactional.service;

import com.finance.transactional.dto.VendorDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.ap.Vendor;
import com.finance.transactional.event.DomainEventPublisher;
import com.finance.transactional.mapper.VendorMapper;
import com.finance.transactional.repository.VendorRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository repository;
    private final VendorMapper mapper;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public VendorDto createVendor(UUID tenantId, VendorDto dto) {
        Vendor vendor = mapper.toEntity(dto);
        vendor.setTenantId(tenantId);
        Vendor saved = repository.save(vendor);
        VendorDto resultDto = mapper.toDto(saved);

        // Publish event
        domainEventPublisher.publish("vendor-created", resultDto);

        return resultDto;
    }

    @Transactional
    public VendorDto updateVendor(UUID tenantId, UUID id, VendorDto dto) {
        Vendor existing = getExistingVendor(tenantId, id);
        Vendor updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public VendorDto getVendorById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingVendor(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<VendorDto> getAllVendors(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteVendor(UUID tenantId, UUID id) {
        Vendor vendor = getExistingVendor(tenantId, id);
        repository.delete(vendor);
    }

    private Vendor getExistingVendor(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id " + id));
    }
}
