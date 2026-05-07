package com.finance.transactional.service;

import com.finance.transactional.dto.ReceiptDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.ar.Receipt;
import com.finance.transactional.mapper.ReceiptMapper;
import com.finance.transactional.repository.ReceiptRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository repository;
    private final ReceiptMapper mapper;

    @Transactional
    public ReceiptDto createReceipt(UUID tenantId, ReceiptDto dto) {
        Receipt receipt = mapper.toEntity(dto);
        receipt.setTenantId(tenantId);
        Receipt saved = repository.save(receipt);
        return mapper.toDto(saved);
    }

    @Transactional
    public ReceiptDto updateReceipt(UUID tenantId, UUID id, ReceiptDto dto) {
        Receipt existing = getExistingReceipt(tenantId, id);
        Receipt updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public ReceiptDto getReceiptById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingReceipt(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<ReceiptDto> getAllReceipts(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteReceipt(UUID tenantId, UUID id) {
        Receipt receipt = getExistingReceipt(tenantId, id);
        repository.delete(receipt);
    }

    private Receipt getExistingReceipt(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with id " + id));
    }
}
