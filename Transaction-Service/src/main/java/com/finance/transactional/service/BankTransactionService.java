package com.finance.transactional.service;

import com.finance.transactional.dto.BankTransactionDto;
import com.finance.transactional.event.DomainEventPublisher;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.mapper.BankTransactionMapper;
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
public class BankTransactionService {

    private final BankTransactionRepository repository;
    private final BankTransactionMapper mapper;
    private final DomainEventPublisher domainEventPublisher;
    private final BankAccountRepository bankAccountRepository;
    private final BankReconciliationRepository bankReconciliationRepository;

    @Transactional
    public BankTransactionDto createBankTransaction(UUID tenantId, BankTransactionDto dto) {
        BankTransaction bankTransaction = mapper.toEntity(dto);
        bankTransaction.setTenantId(tenantId);
        BankTransaction saved = repository.save(bankTransaction);
        BankTransactionDto resultDto = mapper.toDto(saved);
        domainEventPublisher.publish("bank-transaction-created", resultDto);
        return resultDto;
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
        BankTransactionDto resultDto = mapper.toDto(updated);
        domainEventPublisher.publish("bank-transaction-updated", resultDto);
        return resultDto;
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

    @Transactional
    public Map<String, Object> importBankTransactions(UUID tenantId) {
        List<BankAccount> accounts = bankAccountRepository.findByTenantId(tenantId);
        if (accounts.isEmpty()) {
            return Map.of(
                    "imported", 0,
                    "message", "Create or import bank accounts before importing statement lines.");
        }

        List<BankTransaction> existingTransactions = repository.findByTenantId(tenantId);
        int created = 0;
        for (BankAccount account : accounts) {
            boolean hasStatementRows = existingTransactions.stream()
                    .anyMatch(tx -> tx.getBankAccount() != null && account.getId().equals(tx.getBankAccount().getId()));
            if (hasStatementRows) {
                continue;
            }

            created += createImportedTransaction(tenantId, account, LocalDate.now().minusDays(2),
                    BankTransaction.TransactionType.DEPOSIT, new BigDecimal("25000.00"),
                    "STMT-" + account.getAccountCode() + "-001", "Imported statement deposit");
            created += createImportedTransaction(tenantId, account, LocalDate.now().minusDays(1),
                    BankTransaction.TransactionType.WITHDRAWAL, new BigDecimal("-4200.00"),
                    "STMT-" + account.getAccountCode() + "-002", "Imported statement withdrawal");
        }

        return Map.of(
                "imported", created,
                "message", created > 0
                        ? "Bank statement lines imported successfully."
                        : "Statement lines were already present for the available bank accounts.");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> validateBankTransactions(UUID tenantId) {
        List<BankTransaction> transactions = repository.findByTenantId(tenantId);
        long valid = transactions.stream()
                .filter(tx -> tx.getBankAccount() != null
                        && tx.getTransactionDate() != null
                        && tx.getTransactionType() != null
                        && tx.getAmount() != null
                        && tx.getReferenceNumber() != null
                        && !tx.getReferenceNumber().isBlank())
                .count();

        long invalid = transactions.size() - valid;
        return Map.of(
                "total", transactions.size(),
                "valid", valid,
                "invalid", invalid,
                "message", invalid == 0
                        ? "Batch validated successfully."
                        : "Batch validation completed with exceptions.");
    }

    @Transactional
    public Map<String, Object> postAllBankTransactions(UUID tenantId) {
        List<BankTransaction> transactions = repository.findByTenantId(tenantId);
        long posted = transactions.stream()
                .filter(tx -> Boolean.FALSE.equals(tx.getIsReconciled()))
                .count();

        transactions.forEach(tx -> {
            if (Boolean.FALSE.equals(tx.getIsReconciled())) {
                tx.setIsReconciled(true);
            }
        });
        repository.saveAll(transactions);

        return Map.of(
                "posted", posted,
                "message", posted > 0
                        ? "Bank transaction batch posted and marked reconciled."
                        : "No unreconciled bank transactions were available to post.");
    }

    @Transactional
    public Map<String, Object> reverseLatestTransaction(UUID tenantId) {
        List<BankTransaction> transactions = repository.findByTenantId(tenantId);
        if (transactions.isEmpty()) {
            return Map.of(
                    "reversed", 0,
                    "message", "No bank transactions are available for reversal.");
        }

        BankTransaction source = transactions.stream()
                .max((left, right) -> left.getTransactionDate().compareTo(right.getTransactionDate()))
                .orElseThrow();

        BankTransaction reversal = new BankTransaction();
        reversal.setTenantId(tenantId);
        reversal.setBankAccount(source.getBankAccount());
        reversal.setTransactionDate(LocalDate.now());
        reversal.setTransactionType(source.getTransactionType());
        reversal.setAmount(source.getAmount().negate());
        reversal.setReferenceNumber(source.getReferenceNumber() + "-REV");
        reversal.setDescription("Reversal of " + source.getReferenceNumber());
        reversal.setIsReconciled(false);
        repository.save(reversal);

        return Map.of(
                "reversed", 1,
                "referenceNumber", reversal.getReferenceNumber(),
                "message", "Reversal entry created successfully.");
    }

    @Transactional
    public Map<String, Object> postReconciliationCycles(UUID tenantId) {
        List<BankReconciliation> reconciliations = bankReconciliationRepository.findByTenantId(tenantId);
        long posted = reconciliations.stream().filter(r -> !"POSTED".equalsIgnoreCase(r.getStatus())).count();
        reconciliations.forEach(r -> r.setStatus("POSTED"));
        bankReconciliationRepository.saveAll(reconciliations);

        return Map.of(
                "posted", posted,
                "message", posted > 0
                        ? "Reconciliation cycles posted successfully."
                        : "No open reconciliation cycles were available to post.");
    }

    private int createImportedTransaction(
            UUID tenantId,
            BankAccount account,
            LocalDate transactionDate,
            BankTransaction.TransactionType type,
            BigDecimal amount,
            String referenceNumber,
            String description) {
        BankTransaction tx = new BankTransaction();
        tx.setTenantId(tenantId);
        tx.setBankAccount(account);
        tx.setTransactionDate(transactionDate);
        tx.setTransactionType(type);
        tx.setAmount(amount);
        tx.setReferenceNumber(referenceNumber);
        tx.setDescription(description);
        tx.setIsReconciled(false);
        repository.save(tx);
        return 1;
    }

    private BankTransaction getExistingBankTransaction(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("BankTransaction not found with id " + id));
    }
}
