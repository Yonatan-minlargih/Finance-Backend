package com.finance.transactional.service;

import com.finance.transactional.dto.BankAccountDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.mapper.BankAccountMapper;
import com.finance.transactional.model.banking.BankAccount;
import com.finance.transactional.repository.BankAccountRepository;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @Transactional
    public Map<String, Object> importBankAccounts(UUID tenantId) {
        List<BankAccount> existing = repository.findByTenantId(tenantId);
        int created = 0;

        if (existing.isEmpty()) {
            BankAccount main = new BankAccount();
            main.setTenantId(tenantId);
            main.setAccountCode("BANK-001");
            main.setBankName("Commercial Bank");
            main.setAccountNumber("1000000001");
            main.setCurrency("ETB");
            main.setCurrentBalance(new BigDecimal("1250000.00"));
            main.setIsActive(true);

            BankAccount reserve = new BankAccount();
            reserve.setTenantId(tenantId);
            reserve.setAccountCode("BANK-002");
            reserve.setBankName("Development Bank");
            reserve.setAccountNumber("1000000002");
            reserve.setCurrency("USD");
            reserve.setCurrentBalance(new BigDecimal("87500.00"));
            reserve.setIsActive(true);

            repository.saveAll(List.of(main, reserve));
            created = 2;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("created", created);
        result.put("totalAccounts", repository.findByTenantId(tenantId).size());
        result.put("message", created > 0
                ? "Bank accounts imported successfully."
                : "Bank accounts already exist; nothing new was imported.");
        return result;
    }

    @Transactional
    public Map<String, Object> bulkToggleStatus(UUID tenantId) {
        List<BankAccount> accounts = repository.findByTenantId(tenantId);
        if (accounts.isEmpty()) {
            return Map.of(
                    "updated", 0,
                    "activeAccounts", 0,
                    "message", "No bank accounts found to bulk update.");
        }

        boolean hasInactive = accounts.stream().anyMatch(a -> !Boolean.TRUE.equals(a.getIsActive()));
        accounts.forEach(account -> account.setIsActive(hasInactive));
        repository.saveAll(accounts);

        long activeCount = accounts.stream().filter(a -> Boolean.TRUE.equals(a.getIsActive())).count();
        return Map.of(
                "updated", accounts.size(),
                "activeAccounts", activeCount,
                "message", hasInactive
                        ? "All bank accounts released to active status."
                        : "All bank accounts placed on hold.");
    }

    private BankAccount getExistingBankAccount(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount not found with id " + id));
    }
}
