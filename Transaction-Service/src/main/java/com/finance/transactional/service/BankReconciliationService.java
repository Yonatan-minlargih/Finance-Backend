package com.finance.transactional.service;

import com.finance.transactional.dto.BankReconciliationDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.banking.BankReconciliation;
import com.finance.transactional.mapper.BankReconciliationMapper;
import com.finance.transactional.repository.BankReconciliationRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BankReconciliationService {

    private final BankReconciliationRepository repository;
    private final BankReconciliationMapper mapper;

    @Transactional
    public BankReconciliationDto createBankReconciliation(UUID tenantId, BankReconciliationDto dto) {
        BankReconciliation bankReconciliation = mapper.toEntity(dto);
        bankReconciliation.setTenantId(tenantId);
        BankReconciliation saved = repository.save(bankReconciliation);
        return mapper.toDto(saved);
    }

    @Transactional
    public BankReconciliationDto updateBankReconciliation(UUID tenantId, UUID id, BankReconciliationDto dto) {
        BankReconciliation existing = getExistingBankReconciliation(tenantId, id);
        BankReconciliation updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public BankReconciliationDto getBankReconciliationById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingBankReconciliation(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<BankReconciliationDto> getAllBankReconciliations(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteBankReconciliation(UUID tenantId, UUID id) {
        BankReconciliation bankReconciliation = getExistingBankReconciliation(tenantId, id);
        repository.delete(bankReconciliation);
    }

    private BankReconciliation getExistingBankReconciliation(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("BankReconciliation not found with id " + id));
    }
}
