package com.finance.transactional.service;

import com.finance.transactional.dto.VendorAddressDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.ap.VendorAddress;
import com.finance.transactional.mapper.VendorAddressMapper;
import com.finance.transactional.repository.VendorAddressRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VendorAddressService {

    private final VendorAddressRepository repository;
    private final VendorAddressMapper mapper;

    @Transactional
    public VendorAddressDto createVendorAddress(UUID tenantId, VendorAddressDto dto) {
        VendorAddress vendorAddress = mapper.toEntity(dto);
        vendorAddress.setTenantId(tenantId);
        VendorAddress saved = repository.save(vendorAddress);
        return mapper.toDto(saved);
    }

    @Transactional
    public VendorAddressDto updateVendorAddress(UUID tenantId, UUID id, VendorAddressDto dto) {
        VendorAddress existing = getExistingVendorAddress(tenantId, id);
        VendorAddress updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public VendorAddressDto getVendorAddressById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingVendorAddress(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<VendorAddressDto> getAllVendorAddresss(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteVendorAddress(UUID tenantId, UUID id) {
        VendorAddress vendorAddress = getExistingVendorAddress(tenantId, id);
        repository.delete(vendorAddress);
    }

    private VendorAddress getExistingVendorAddress(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("VendorAddress not found with id " + id));
    }
}
