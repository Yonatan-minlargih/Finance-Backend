package com.finance.transactional.service;

import com.finance.transactional.dto.ReceiptAllocationDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.ar.ReceiptAllocation;
import com.finance.transactional.mapper.ReceiptAllocationMapper;
import com.finance.transactional.repository.ReceiptAllocationRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReceiptAllocationService {

    private final ReceiptAllocationRepository repository;
    private final ReceiptAllocationMapper mapper;

    @Transactional
    public ReceiptAllocationDto createReceiptAllocation(UUID tenantId, ReceiptAllocationDto dto) {
        ReceiptAllocation receiptAllocation = mapper.toEntity(dto);
        receiptAllocation.setTenantId(tenantId);
        ReceiptAllocation saved = repository.save(receiptAllocation);
        return mapper.toDto(saved);
    }

    @Transactional
    public ReceiptAllocationDto updateReceiptAllocation(UUID tenantId, UUID id, ReceiptAllocationDto dto) {
        ReceiptAllocation existing = getExistingReceiptAllocation(tenantId, id);
        ReceiptAllocation updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public ReceiptAllocationDto getReceiptAllocationById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingReceiptAllocation(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<ReceiptAllocationDto> getAllReceiptAllocations(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteReceiptAllocation(UUID tenantId, UUID id) {
        ReceiptAllocation receiptAllocation = getExistingReceiptAllocation(tenantId, id);
        repository.delete(receiptAllocation);
    }

    private ReceiptAllocation getExistingReceiptAllocation(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("ReceiptAllocation not found with id " + id));
    }
}
