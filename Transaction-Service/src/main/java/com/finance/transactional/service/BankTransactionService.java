package com.finance.transactional.service;

import com.finance.transactional.dto.BankTransactionDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.banking.BankTransaction;
import com.finance.transactional.mapper.BankTransactionMapper;
import com.finance.transactional.repository.BankTransactionRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BankTransactionService {

    private final BankTransactionRepository repository;
    private final BankTransactionMapper mapper;

    @Transactional
    public BankTransactionDto createBankTransaction(UUID tenantId, BankTransactionDto dto) {
        BankTransaction bankTransaction = mapper.toEntity(dto);
        bankTransaction.setTenantId(tenantId);
        BankTransaction saved = repository.save(bankTransaction);
        return mapper.toDto(saved);
    }

    @Transactional
    public BankTransactionDto updateBankTransaction(UUID tenantId, UUID id, BankTransactionDto dto) {
        BankTransaction existing = getExistingBankTransaction(tenantId, id);
        BankTransaction updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public BankTransactionDto getBankTransactionById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingBankTransaction(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<BankTransactionDto> getAllBankTransactions(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteBankTransaction(UUID tenantId, UUID id) {
        BankTransaction bankTransaction = getExistingBankTransaction(tenantId, id);
        repository.delete(bankTransaction);
    }

    private BankTransaction getExistingBankTransaction(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("BankTransaction not found with id " + id));
    }
}
