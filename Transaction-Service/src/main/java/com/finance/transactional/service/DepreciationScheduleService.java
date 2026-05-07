package com.finance.transactional.service;

import com.finance.transactional.dto.DepreciationScheduleDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.asset.DepreciationSchedule;
import com.finance.transactional.mapper.DepreciationScheduleMapper;
import com.finance.transactional.repository.DepreciationScheduleRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepreciationScheduleService {

    private final DepreciationScheduleRepository repository;
    private final DepreciationScheduleMapper mapper;

    @Transactional
    public DepreciationScheduleDto createDepreciationSchedule(UUID tenantId, DepreciationScheduleDto dto) {
        DepreciationSchedule depreciationSchedule = mapper.toEntity(dto);
        depreciationSchedule.setTenantId(tenantId);
        DepreciationSchedule saved = repository.save(depreciationSchedule);
        return mapper.toDto(saved);
    }

    @Transactional
    public DepreciationScheduleDto updateDepreciationSchedule(UUID tenantId, UUID id, DepreciationScheduleDto dto) {
        DepreciationSchedule existing = getExistingDepreciationSchedule(tenantId, id);
        DepreciationSchedule updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public DepreciationScheduleDto getDepreciationScheduleById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingDepreciationSchedule(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<DepreciationScheduleDto> getAllDepreciationSchedules(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteDepreciationSchedule(UUID tenantId, UUID id) {
        DepreciationSchedule depreciationSchedule = getExistingDepreciationSchedule(tenantId, id);
        repository.delete(depreciationSchedule);
    }

    private DepreciationSchedule getExistingDepreciationSchedule(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("DepreciationSchedule not found with id " + id));
    }
}
