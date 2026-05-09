package com.finance.transactional.service;

import com.finance.transactional.dto.FixedAssetDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.asset.FixedAsset;
import com.finance.transactional.event.DomainEventPublisher;
import com.finance.transactional.mapper.FixedAssetMapper;
import com.finance.transactional.repository.FixedAssetRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FixedAssetService {

    private final FixedAssetRepository repository;
    private final FixedAssetMapper mapper;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public FixedAssetDto createFixedAsset(UUID tenantId, FixedAssetDto dto) {
        FixedAsset fixedAsset = mapper.toEntity(dto);
        fixedAsset.setTenantId(tenantId);
        FixedAsset saved = repository.save(fixedAsset);
        FixedAssetDto resultDto = mapper.toDto(saved);

        // Publish event
        domainEventPublisher.publish("fixed-asset-created", resultDto);

        return resultDto;
    }

    @Transactional
    public FixedAssetDto updateFixedAsset(UUID tenantId, UUID id, FixedAssetDto dto) {
        FixedAsset existing = getExistingFixedAsset(tenantId, id);
        FixedAsset updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public FixedAssetDto getFixedAssetById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingFixedAsset(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<FixedAssetDto> getAllFixedAssets(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteFixedAsset(UUID tenantId, UUID id) {
        FixedAsset fixedAsset = getExistingFixedAsset(tenantId, id);
        repository.delete(fixedAsset);
    }

    private FixedAsset getExistingFixedAsset(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedAsset not found with id " + id));
    }
}
