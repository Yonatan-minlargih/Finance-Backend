package com.financial.corefinance.service;

import com.financial.corefinance.domain.entity.Account;
import com.financial.corefinance.exception.AccountValidationException;
import com.financial.corefinance.repository.AccountRepository;
import com.financial.corefinance.event.FinanceEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final FinanceEventService financeEventService;

    @Transactional
    public Account createAccount(@Valid Account account) {
        log.info("Creating account: {}", account.getAccountCode());
        
        // Validate parent account exists if specified
        if (account.getParentAccountId() != null) {
            Optional<Account> parentAccount = accountRepository.findById(account.getParentAccountId());
            if (parentAccount.isEmpty()) {
                throw new AccountValidationException("Parent account not found: " + account.getParentAccountId());
            }
            
            // Validate parent is active
            if (!parentAccount.get().getIsActive()) {
                throw new AccountValidationException("Parent account is not active: " + parentAccount.get().getAccountCode());
            }
        }
        
        // Validate account code uniqueness
        String tenantId = account.getTenantId();
        if (accountRepository.existsByTenantIdAndAccountCode(tenantId, account.getAccountCode())) {
            throw new AccountValidationException("Account with code " + account.getAccountCode() + " already exists");
        }
        
        // Set default values
        if (account.getCurrencyCode() == null) {
            account.setCurrencyCode("USD");
        }
        if (account.getIsActive() == null) {
            account.setIsActive(true);
        }
        if (account.getAllowManualEntry() == null) {
            account.setAllowManualEntry(true);
        }
        if (account.getReconciliationRequired() == null) {
            account.setReconciliationRequired(false);
        }
        
        // Calculate account level
        calculateAccountLevel(account);
        
        Account savedAccount = accountRepository.save(account);
        
        // Publish account created event
        financeEventService.publishAccountCreatedEvent(savedAccount.getId(), savedAccount.getTenantId(), savedAccount.getCreatedBy());
        
        log.info("Account created successfully: {}", savedAccount.getAccountCode());
        return savedAccount;
    }

    @Transactional
    public Account updateAccount(@Valid Account account) {
        log.info("Updating account: {}", account.getAccountCode());
        
        Optional<Account> existingAccountOpt = accountRepository.findById(account.getId());
        if (existingAccountOpt.isEmpty()) {
            throw new AccountValidationException("Account not found: " + account.getId());
        }
        
        Account existingAccount = existingAccountOpt.get();
        
        // Validate cannot change parent to a child account (circular reference)
        if (account.getParentAccountId() != null && !account.getParentAccountId().equals(existingAccount.getParentAccountId())) {
            validateNoCircularReference(account.getId(), account.getParentAccountId());
        }
        
        // Update fields
        existingAccount.setAccountName(account.getAccountName());
        existingAccount.setAccountType(account.getAccountType());
        existingAccount.setNormalBalance(account.getNormalBalance());
        existingAccount.setParentAccountId(account.getParentAccountId());
        existingAccount.setIFRSCategory(account.getIFRSCategory());
        existingAccount.setDescription(account.getDescription());
        existingAccount.setIsActive(account.getIsActive());
        existingAccount.setIsConsolidated(account.getIsConsolidated());
        existingAccount.setAccountFormat(account.getAccountFormat());
        existingAccount.setMinLength(account.getMaxLength());
        existingAccount.setMaxLength(account.getMaxLength());
        existingAccount.setAllowManualEntry(account.getAllowManualEntry());
        existingAccount.setReconciliationRequired(account.getReconciliationRequired());
        existingAccount.setOpeningBalance(account.getOpeningBalance());
        existingAccount.setOpeningBalanceDate(account.getOpeningBalanceDate());
        existingAccount.setCurrencyCode(account.getCurrencyCode());
        
        // Recalculate level
        calculateAccountLevel(existingAccount);
        
        Account updatedAccount = accountRepository.save(existingAccount);
        log.info("Account updated successfully: {}", updatedAccount.getAccountCode());
        return updatedAccount;
    }

    @Transactional(readOnly = true)
    public Optional<Account> getAccountById(UUID accountId) {
        return accountRepository.findById(accountId);
    }

    @Transactional(readOnly = true)
    public Optional<Account> getAccountByCode(String tenantId, String accountCode) {
        return accountRepository.findByTenantIdAndAccountCode(tenantId, accountCode);
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountHierarchy(String tenantId) {
        return accountRepository.findByTenantIdAndParentAccountIdIsNull(tenantId);
    }

    @Transactional(readOnly = true)
    public List<Account> getSubAccounts(String tenantId, UUID parentAccountId) {
        return accountRepository.findByTenantIdAndParentAccountId(tenantId, parentAccountId);
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountsByType(String tenantId, Account.AccountType accountType) {
        return accountRepository.findByTenantIdAndAccountType(tenantId, accountType);
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountsByIFRSCategory(String tenantId, Account.IFRSCategory ifrsCategory) {
        return accountRepository.findByTenantIdAndIfrsCategory(tenantId, ifrsCategory);
    }

    @Transactional(readOnly = true)
    public List<Account> getActiveAccounts(String tenantId) {
        return accountRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountsForManualEntry(String tenantId) {
        return accountRepository.findAccountsForManualEntry(tenantId);
    }

    @Transactional(readOnly = true)
    public List<Account> getReconciliationRequiredAccounts(String tenantId) {
        return accountRepository.findReconciliationRequiredAccounts(tenantId);
    }

    @Transactional(readOnly = true)
    public List<Account> searchAccounts(String tenantId, String searchTerm) {
        return accountRepository.searchAccounts(tenantId, searchTerm);
    }

    @Transactional
    public void deleteAccount(UUID accountId) {
        log.info("Deleting account: {}", accountId);
        
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            throw new AccountValidationException("Account not found: " + accountId);
        }
        
        Account account = accountOpt.get();
        
        // Check if account has sub-accounts
        List<Account> subAccounts = accountRepository.findByTenantIdAndParentAccountId(account.getTenantId(), accountId);
        if (!subAccounts.isEmpty()) {
            throw new AccountValidationException("Cannot delete account with sub-accounts. Delete sub-accounts first.");
        }
        
        // Additional validation would be needed to check if account is used in transactions
        // This would involve checking journal lines
        
        accountRepository.deleteById(accountId);
        log.info("Account deleted successfully: {}", accountId);
    }

    @Transactional
    public Account activateAccount(UUID accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            throw new AccountValidationException("Account not found: " + accountId);
        }
        
        Account account = accountOpt.get();
        account.setIsActive(true);
        return accountRepository.save(account);
    }

    @Transactional
    public Account deactivateAccount(UUID accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            throw new AccountValidationException("Account not found: " + accountId);
        }
        
        Account account = accountOpt.get();
        account.setIsActive(false);
        return accountRepository.save(account);
    }

    private void calculateAccountLevel(Account account) {
        if (account.getParentAccountId() != null) {
            Optional<Account> parentAccount = accountRepository.findById(account.getParentAccountId());
            if (parentAccount.isPresent()) {
                account.setLevel(parentAccount.get().getLevel() != null ? parentAccount.get().getLevel() + 1 : 1);
            } else {
                account.setLevel(0);
            }
        } else {
            account.setLevel(0);
        }
    }

    private void validateNoCircularReference(UUID accountId, UUID parentAccountId) {
        UUID currentParentId = parentAccountId;
        int maxDepth = 10; // Prevent infinite loops
        
        while (currentParentId != null && maxDepth-- > 0) {
            if (currentParentId.equals(accountId)) {
                throw new AccountValidationException("Circular reference detected: Account cannot be its own ancestor");
            }
            
            Optional<Account> parentAccount = accountRepository.findById(currentParentId);
            if (parentAccount.isEmpty()) {
                break;
            }
            
            currentParentId = parentAccount.get().getParentAccountId();
        }
        
        if (maxDepth <= 0) {
            throw new AccountValidationException("Account hierarchy too deep, possible circular reference");
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal getAccountBalance(UUID accountId, UUID fiscalYearId, UUID periodId) {
        // This would calculate the balance for an account up to a specific period
        // Implementation would involve summing journal lines for the account
        // This is a placeholder for the actual implementation
        
        log.debug("Calculating balance for account: {} in fiscal year: {} period: {}", 
                 accountId, fiscalYearId, periodId);
        
        // Placeholder implementation
        return BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public boolean validateAccountCode(String tenantId, String accountCode) {
        // Validate account code format and uniqueness
        if (accountCode == null || accountCode.trim().isEmpty()) {
            return false;
        }
        
        // Check if account already exists
        return !accountRepository.existsByTenantIdAndAccountCode(tenantId, accountCode);
    }
}
