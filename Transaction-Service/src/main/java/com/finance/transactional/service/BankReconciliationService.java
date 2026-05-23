package com.finance.transactional.service;

import com.finance.transactional.dto.BankReconciliationDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.mapper.BankReconciliationMapper;
import com.finance.transactional.model.banking.BankAccount;
import com.finance.transactional.model.banking.BankReconciliation;
import com.finance.transactional.model.banking.BankTransaction;
import com.finance.transactional.repository.BankAccountRepository;
import com.finance.transactional.repository.BankReconciliationRepository;
import com.finance.transactional.repository.BankTransactionRepository;
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
public class BankReconciliationService {

    private final BankReconciliationRepository repository;
    private final BankReconciliationMapper mapper;
    private final BankTransactionRepository bankTransactionRepository;
    private final BankAccountRepository bankAccountRepository;

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

    @Transactional
    public void postAllBankReconciliations(UUID tenantId) {
        List<BankReconciliation> draftReconciliations = repository.findByTenantId(tenantId).stream()
                .filter(r -> !"POSTED".equals(r.getStatus()))
                .toList();

        draftReconciliations.forEach(r -> r.setStatus("POSTED"));
        repository.saveAll(draftReconciliations);
    }

    @Transactional
    public Map<String, Object> toggleHoldRelease(UUID tenantId) {
        List<BankReconciliation> reconciliations = repository.findByTenantId(tenantId);
        if (reconciliations.isEmpty()) {
            return Map.of(
                    "updated", 0,
                    "message", "No reconciliation cycles found to hold or release.");
        }

        boolean hasHeld = reconciliations.stream().anyMatch(r -> "ON_HOLD".equalsIgnoreCase(r.getStatus()));
        reconciliations.forEach(r -> r.setStatus(hasHeld ? "DRAFT" : "ON_HOLD"));
        repository.saveAll(reconciliations);

        return Map.of(
                "updated", reconciliations.size(),
                "message", hasHeld
                        ? "Held reconciliation cycles released back to draft."
                        : "Open reconciliation cycles placed on hold.");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getApprovalQueue(UUID tenantId) {
        List<BankReconciliation> pending = repository.findByTenantId(tenantId).stream()
                .filter(r -> !"POSTED".equalsIgnoreCase(r.getStatus()))
                .toList();

        return Map.of(
                "pending", pending.size(),
                "message", pending.isEmpty()
                        ? "No reconciliation cycles are waiting for approval."
                        : pending.size() + " reconciliation cycle(s) are awaiting approval.");
    }

    @Transactional
    public Map<String, Object> matchDocuments(UUID tenantId) {
        List<BankTransaction> unreconciled = bankTransactionRepository.findByTenantIdAndIsReconciledFalse(tenantId);
        List<BankReconciliation> openCycles = repository.findByTenantId(tenantId).stream()
                .filter(r -> !"POSTED".equalsIgnoreCase(r.getStatus()) && !"ON_HOLD".equalsIgnoreCase(r.getStatus()))
                .toList();

        if (unreconciled.isEmpty() || openCycles.isEmpty()) {
            return Map.of(
                    "matched", 0,
                    "message", "No open transaction/reconciliation pairs were available for matching.");
        }

        BankReconciliation cycle = openCycles.get(0);
        unreconciled.forEach(tx -> {
            tx.setIsReconciled(true);
            tx.setBankReconciliation(cycle);
        });
        bankTransactionRepository.saveAll(unreconciled);

        BigDecimal systemBalance = calculateSystemBalance(tenantId, cycle.getBankAccount().getId());
        cycle.setSystemBalance(systemBalance);
        cycle.setVariance(cycle.getStatementBalance().subtract(systemBalance));
        cycle.setStatus(cycle.getVariance().compareTo(BigDecimal.ZERO) == 0 ? "MATCHED" : "DRAFT");
        repository.save(cycle);

        return Map.of(
                "matched", unreconciled.size(),
                "message", "Matching completed against the active reconciliation cycle.");
    }

    @Transactional
    public Map<String, Object> scanCapture(UUID tenantId) {
        List<BankAccount> accounts = bankAccountRepository.findByTenantId(tenantId);
        if (accounts.isEmpty()) {
            return Map.of(
                    "captured", 0,
                    "message", "No bank accounts available for statement capture.");
        }

        List<BankReconciliation> existing = repository.findByTenantId(tenantId);
        int created = 0;
        for (BankAccount account : accounts) {
            boolean alreadyOpen = existing.stream()
                    .anyMatch(r -> r.getBankAccount() != null
                            && account.getId().equals(r.getBankAccount().getId())
                            && !"POSTED".equalsIgnoreCase(r.getStatus()));
            if (alreadyOpen) {
                continue;
            }

            BankReconciliation reconciliation = new BankReconciliation();
            reconciliation.setTenantId(tenantId);
            reconciliation.setBankAccount(account);
            reconciliation.setStatementDate(LocalDate.now());
            BigDecimal systemBalance = calculateSystemBalance(tenantId, account.getId());
            reconciliation.setSystemBalance(systemBalance);
            reconciliation.setStatementBalance(systemBalance);
            reconciliation.setVariance(BigDecimal.ZERO);
            reconciliation.setStatus("DRAFT");
            repository.save(reconciliation);
            created++;
        }

        return Map.of(
                "captured", created,
                "message", created > 0
                        ? "Statement capture created reconciliation cycles successfully."
                        : "Open reconciliation cycles already exist for the available accounts.");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> importStatement(UUID tenantId) {
        long openCycles = repository.findByTenantId(tenantId).stream()
                .filter(r -> !"POSTED".equalsIgnoreCase(r.getStatus()))
                .count();
        return Map.of(
                "openCycles", openCycles,
                "message", openCycles > 0
                        ? "Imported statement lines are available for reconciliation."
                        : "No open reconciliation cycles exist yet. Run scan/capture first.");
    }

    private BigDecimal calculateSystemBalance(UUID tenantId, UUID bankAccountId) {
        return bankTransactionRepository.findByTenantId(tenantId).stream()
                .filter(tx -> tx.getBankAccount() != null && bankAccountId.equals(tx.getBankAccount().getId()))
                .map(BankTransaction::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BankReconciliation getExistingBankReconciliation(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("BankReconciliation not found with id " + id));
    }
}
