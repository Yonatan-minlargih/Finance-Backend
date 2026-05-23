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

import com.finance.transactional.model.ap.VendorAddress;
import com.finance.transactional.dto.VendorAddressDto;

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
        if (vendor.getAddresses() != null) {
            vendor.getAddresses().forEach(addr -> {
                addr.setVendor(vendor);
                addr.setTenantId(tenantId);
            });
        }
        Vendor saved = repository.save(vendor);
        VendorDto resultDto = mapper.toDto(saved);

        // Publish event
        domainEventPublisher.publish("vendor-created", resultDto);

        return resultDto;
    }

    @Transactional
    public VendorDto updateVendor(UUID tenantId, UUID id, VendorDto dto) {
        Vendor existing = getExistingVendor(tenantId, id);
        existing.setVendorCode(dto.getVendorCode());
        existing.setVendorName(dto.getVendorName());
        existing.setTaxId(dto.getTaxId());
        existing.setContactEmail(dto.getContactEmail());
        existing.setContactPhone(dto.getContactPhone());
        existing.setPaymentTerms(dto.getPaymentTerms());
        existing.setIsActive(dto.getIsActive());
        existing.setClassification(dto.getClassification());
        existing.setPaymentPriority(dto.getPaymentPriority());
        existing.setEmdWaiver(dto.getEmdWaiver());
        existing.setContactPerson(dto.getContactPerson());
        existing.setVatNumber(dto.getVatNumber());
        existing.setBankAccountNumber(dto.getBankAccountNumber());
        existing.setDefaultCurrency(dto.getDefaultCurrency());


        if (dto.getAddresses() != null) {
            existing.getAddresses().clear();
            for (VendorAddressDto addrDto : dto.getAddresses()) {
                VendorAddress addr = new VendorAddress();
                addr.setVendor(existing);
                addr.setAddressType(addrDto.getAddressType());
                addr.setStreetAddress(addrDto.getStreetAddress());
                addr.setCity(addrDto.getCity());
                addr.setState(addrDto.getState());
                addr.setPostalCode(addrDto.getPostalCode());
                addr.setCountry(addrDto.getCountry());
                addr.setTenantId(tenantId);
                existing.getAddresses().add(addr);
            }
        }

        Vendor saved = repository.save(existing);
        return mapper.toDto(saved);
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
