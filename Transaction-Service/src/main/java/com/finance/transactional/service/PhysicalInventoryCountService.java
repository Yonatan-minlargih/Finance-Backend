package com.finance.transactional.service;

import com.finance.transactional.dto.PhysicalInventoryCountDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.mapper.PhysicalInventoryCountMapper;
import com.finance.transactional.model.asset.AssetTransaction;
import com.finance.transactional.model.asset.FixedAsset;
import com.finance.transactional.model.asset.PhysicalInventoryCount;
import com.finance.transactional.repository.AssetTransactionRepository;
import com.finance.transactional.repository.FixedAssetRepository;
import com.finance.transactional.repository.PhysicalInventoryCountRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PhysicalInventoryCountService {

    private final PhysicalInventoryCountRepository repository;
    private final PhysicalInventoryCountMapper mapper;
    private final FixedAssetRepository fixedAssetRepository;
    private final AssetTransactionRepository assetTransactionRepository;

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

    @Transactional
    public Map<String, Object> applyCount(UUID tenantId, UUID id) {
        PhysicalInventoryCount count = getExistingPhysicalInventoryCount(tenantId, id);
        FixedAsset asset = getExistingAsset(tenantId, count.getAsset().getId());
        asset.setLastInventoriedDate(count.getCountDate());

        BigDecimal varianceAmount = BigDecimal.ZERO;

        if (Boolean.TRUE.equals(count.getIsFound())) {
            asset.setStatus(FixedAsset.AssetStatus.ACTIVE);
        } else {
            asset.setStatus(FixedAsset.AssetStatus.IN_MAINTENANCE);
            varianceAmount = safe(asset.getNetBookValue()).negate();
        }
        fixedAssetRepository.save(asset);

        AssetTransaction transaction = new AssetTransaction();
        transaction.setTenantId(tenantId);
        transaction.setAsset(asset);
        transaction.setTransactionType(Boolean.TRUE.equals(count.getIsFound()) ? "PHYSICAL_COUNT" : "INVENTORY_VARIANCE");
        transaction.setTransactionDate(count.getCountDate());
        transaction.setAmount(varianceAmount);
        transaction.setDescription(Boolean.TRUE.equals(count.getIsFound())
                ? "Physical inventory count confirmed by " + count.getCountedBy()
                : "Physical inventory variance recorded by " + count.getCountedBy());
        assetTransactionRepository.save(transaction);

        return Map.of(
                "message", "Physical count adjustment applied successfully.",
                "varianceAmount", varianceAmount,
                "lastInventoriedDate", count.getCountDate());
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private FixedAsset getExistingAsset(UUID tenantId, UUID id) {
        return fixedAssetRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedAsset not found with id " + id));
    }

    private PhysicalInventoryCount getExistingPhysicalInventoryCount(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("PhysicalInventoryCount not found with id " + id));
    }
}
