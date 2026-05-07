package com.finance.transactional.service;

import com.finance.transactional.dto.BankAccountDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.banking.BankAccount;
import com.finance.transactional.mapper.BankAccountMapper;
import com.finance.transactional.repository.BankAccountRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository repository;
    private final BankAccountMapper mapper;

    @Transactional
    public BankAccountDto createBankAccount(UUID tenantId, BankAccountDto dto) {
        BankAccount bankAccount = mapper.toEntity(dto);
        bankAccount.setTenantId(tenantId);
        BankAccount saved = repository.save(bankAccount);
        return mapper.toDto(saved);
    }

    @Transactional
    public BankAccountDto updateBankAccount(UUID tenantId, UUID id, BankAccountDto dto) {
        BankAccount existing = getExistingBankAccount(tenantId, id);
        BankAccount updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public BankAccountDto getBankAccountById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingBankAccount(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<BankAccountDto> getAllBankAccounts(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteBankAccount(UUID tenantId, UUID id) {
        BankAccount bankAccount = getExistingBankAccount(tenantId, id);
        repository.delete(bankAccount);
    }

    private BankAccount getExistingBankAccount(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount not found with id " + id));
    }
}
