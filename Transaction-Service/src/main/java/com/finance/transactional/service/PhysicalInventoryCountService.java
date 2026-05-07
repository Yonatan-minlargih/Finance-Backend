package com.finance.transactional.service;

import com.finance.transactional.dto.PhysicalInventoryCountDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.asset.PhysicalInventoryCount;
import com.finance.transactional.mapper.PhysicalInventoryCountMapper;
import com.finance.transactional.repository.PhysicalInventoryCountRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PhysicalInventoryCountService {

    private final PhysicalInventoryCountRepository repository;
    private final PhysicalInventoryCountMapper mapper;

    @Transactional
    public PhysicalInventoryCountDto createPhysicalInventoryCount(UUID tenantId, PhysicalInventoryCountDto dto) {
        PhysicalInventoryCount physicalInventoryCount = mapper.toEntity(dto);
        physicalInventoryCount.setTenantId(tenantId);
        PhysicalInventoryCount saved = repository.save(physicalInventoryCount);
        return mapper.toDto(saved);
    }

    @Transactional
    public PhysicalInventoryCountDto updatePhysicalInventoryCount(UUID tenantId, UUID id, PhysicalInventoryCountDto dto) {
        PhysicalInventoryCount existing = getExistingPhysicalInventoryCount(tenantId, id);
        PhysicalInventoryCount updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public PhysicalInventoryCountDto getPhysicalInventoryCountById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingPhysicalInventoryCount(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<PhysicalInventoryCountDto> getAllPhysicalInventoryCounts(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deletePhysicalInventoryCount(UUID tenantId, UUID id) {
        PhysicalInventoryCount physicalInventoryCount = getExistingPhysicalInventoryCount(tenantId, id);
        repository.delete(physicalInventoryCount);
    }

    private PhysicalInventoryCount getExistingPhysicalInventoryCount(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("PhysicalInventoryCount not found with id " + id));
    }
}
