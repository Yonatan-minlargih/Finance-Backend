package com.finance.transactional.service;

import com.finance.transactional.dto.AssetLocationDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.asset.AssetLocation;
import com.finance.transactional.mapper.AssetLocationMapper;
import com.finance.transactional.repository.AssetLocationRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssetLocationService {

    private final AssetLocationRepository repository;
    private final AssetLocationMapper mapper;

    @Transactional
    public AssetLocationDto createAssetLocation(UUID tenantId, AssetLocationDto dto) {
        AssetLocation assetLocation = mapper.toEntity(dto);
        assetLocation.setTenantId(tenantId);
        AssetLocation saved = repository.save(assetLocation);
        return mapper.toDto(saved);
    }

    @Transactional
    public AssetLocationDto updateAssetLocation(UUID tenantId, UUID id, AssetLocationDto dto) {
        AssetLocation existing = getExistingAssetLocation(tenantId, id);
        AssetLocation updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public AssetLocationDto getAssetLocationById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingAssetLocation(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<AssetLocationDto> getAllAssetLocations(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteAssetLocation(UUID tenantId, UUID id) {
        AssetLocation assetLocation = getExistingAssetLocation(tenantId, id);
        repository.delete(assetLocation);
    }

    private AssetLocation getExistingAssetLocation(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("AssetLocation not found with id " + id));
    }
}
