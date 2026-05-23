package com.finance.transactional.service;

import com.finance.transactional.dto.FixedAssetDto;
import com.finance.transactional.event.DomainEventPublisher;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.mapper.FixedAssetMapper;
import com.finance.transactional.model.asset.AssetTransaction;
import com.finance.transactional.model.asset.FixedAsset;
import com.finance.transactional.repository.AssetTransactionRepository;
import com.finance.transactional.repository.FixedAssetRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    private final AssetTransactionRepository assetTransactionRepository;

    @Transactional
    public FixedAssetDto createFixedAsset(UUID tenantId, FixedAssetDto dto) {
        FixedAsset fixedAsset = mapper.toEntity(dto);
        fixedAsset.setTenantId(tenantId);
        if (fixedAsset.getAccumulatedDepreciation() == null) {
            fixedAsset.setAccumulatedDepreciation(BigDecimal.ZERO);
        }
        if (fixedAsset.getNetBookValue() == null) {
            fixedAsset.setNetBookValue(defaultNetBookValue(fixedAsset));
        }
        if (fixedAsset.getStatus() == null) {
            fixedAsset.setStatus(FixedAsset.AssetStatus.ACTIVE);
        }
        FixedAsset saved = repository.save(fixedAsset);
        FixedAssetDto resultDto = mapper.toDto(saved);
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
        if (updated.getAccumulatedDepreciation() == null) {
            updated.setAccumulatedDepreciation(existing.getAccumulatedDepreciation());
        }
        if (updated.getNetBookValue() == null) {
            updated.setNetBookValue(defaultNetBookValue(updated));
        }
        if (updated.getStatus() == null) {
            updated.setStatus(existing.getStatus());
        }
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

    @Transactional
    public Map<String, Object> impairAsset(UUID tenantId, UUID id, BigDecimal amount, String description) {
        FixedAsset asset = getExistingFixedAsset(tenantId, id);
        BigDecimal impairmentAmount = amount == null ? BigDecimal.ZERO : amount.abs();
        asset.setNetBookValue(safe(asset.getNetBookValue()).subtract(impairmentAmount).max(BigDecimal.ZERO));
        asset.setStatus(FixedAsset.AssetStatus.IMPAIRED);
        repository.save(asset);

        createAssetTransaction(tenantId, asset, "IMPAIRMENT", impairmentAmount.negate(), description);
        return Map.of("message", "Asset impairment recorded successfully.");
    }

    @Transactional
    public Map<String, Object> disposeAsset(UUID tenantId, UUID id, String description) {
        FixedAsset asset = getExistingFixedAsset(tenantId, id);
        BigDecimal disposalAmount = safe(asset.getNetBookValue());
        asset.setNetBookValue(BigDecimal.ZERO);
        asset.setStatus(FixedAsset.AssetStatus.DISPOSED);
        repository.save(asset);

        createAssetTransaction(tenantId, asset, "DISPOSAL", disposalAmount.negate(), description);
        return Map.of("message", "Asset disposal recorded successfully.");
    }

    @Transactional
    public Map<String, Object> reclassifyAsset(
            UUID tenantId,
            UUID id,
            String assetCategory,
            String costCenterCode,
            String description,
            LocalDate effectiveDate) {
        FixedAsset asset = getExistingFixedAsset(tenantId, id);
        asset.setAssetCategory(assetCategory);
        asset.setCostCenterCode(costCenterCode);
        asset.setReclassificationDate(effectiveDate);
        repository.save(asset);

        createAssetTransaction(
                tenantId,
                asset,
                "RECLASSIFICATION",
                BigDecimal.ZERO,
                description == null || description.isBlank()
                        ? "Asset reclassified"
                        : description,
                effectiveDate);
        return Map.of("message", "Asset reclassified successfully.");
    }

    private void createAssetTransaction(
            UUID tenantId,
            FixedAsset asset,
            String transactionType,
            BigDecimal amount,
            String description) {
        createAssetTransaction(tenantId, asset, transactionType, amount, description, LocalDate.now());
    }

    private void createAssetTransaction(
            UUID tenantId,
            FixedAsset asset,
            String transactionType,
            BigDecimal amount,
            String description,
            LocalDate transactionDate) {
        AssetTransaction transaction = new AssetTransaction();
        transaction.setTenantId(tenantId);
        transaction.setAsset(asset);
        transaction.setTransactionType(transactionType);
        transaction.setTransactionDate(transactionDate == null ? LocalDate.now() : transactionDate);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        assetTransactionRepository.save(transaction);
    }

    private BigDecimal defaultNetBookValue(FixedAsset asset) {
        return safe(asset.getAcquisitionCost()).subtract(safe(asset.getAccumulatedDepreciation()));
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private FixedAsset getExistingFixedAsset(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedAsset not found with id " + id));
    }
}
