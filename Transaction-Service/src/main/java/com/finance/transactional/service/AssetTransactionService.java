package com.finance.transactional.service;

import com.finance.transactional.dto.AssetTransactionDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.asset.AssetTransaction;
import com.finance.transactional.event.DomainEventPublisher;
import com.finance.transactional.mapper.AssetTransactionMapper;
import com.finance.transactional.repository.AssetTransactionRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssetTransactionService {

    private final AssetTransactionRepository repository;
    private final AssetTransactionMapper mapper;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public AssetTransactionDto createAssetTransaction(UUID tenantId, AssetTransactionDto dto) {
        AssetTransaction assetTransaction = mapper.toEntity(dto);
        assetTransaction.setTenantId(tenantId);
        AssetTransaction saved = repository.save(assetTransaction);
        AssetTransactionDto resultDto = mapper.toDto(saved);

        // Publish event
        domainEventPublisher.publish("asset-transaction-created", resultDto);

        return resultDto;
    }

    @Transactional
    public AssetTransactionDto updateAssetTransaction(UUID tenantId, UUID id, AssetTransactionDto dto) {
        AssetTransaction existing = getExistingAssetTransaction(tenantId, id);
        AssetTransaction updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public AssetTransactionDto getAssetTransactionById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingAssetTransaction(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<AssetTransactionDto> getAllAssetTransactions(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteAssetTransaction(UUID tenantId, UUID id) {
        AssetTransaction assetTransaction = getExistingAssetTransaction(tenantId, id);
        repository.delete(assetTransaction);
    }

    private AssetTransaction getExistingAssetTransaction(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("AssetTransaction not found with id " + id));
    }
}
