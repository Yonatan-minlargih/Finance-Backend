package com.financial.corefinance.service.validation;

import com.financial.corefinance.domain.entity.*;
import com.financial.corefinance.exception.*;
import com.financial.corefinance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessValidationService {

    private final AccountRepository accountRepository;
    private final JournalHeaderRepository journalHeaderRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetLineRepository budgetLineRepository;
    private final AccountingPeriodRepository accountingPeriodRepository;
    private final FiscalYearRepository fiscalYearRepository;

    public void validateJournalForPosting(JournalHeader journalHeader) {
        log.debug("Validating journal for posting: {}", journalHeader.getDescription());
        
        // Basic validations
        validateJournalBasicRules(journalHeader);
        
        // Accounting period validation
        validateAccountingPeriodForJournal(journalHeader);
        
        // Account validations
        validateJournalAccounts(journalHeader);
        
        // Balance validation
        validateJournalBalance(journalHeader);
        
        // Business rule validations
        validateJournalBusinessRules(journalHeader);
        
        // Budget validation
        validateJournalAgainstBudget(journalHeader);
    }

    public void validateAccountForCreation(Account account) {
        log.debug("Validating account for creation: {}", account.getAccountCode());
        
        // Basic validations
        validateAccountBasicRules(account);
        
        // Hierarchy validation
        validateAccountHierarchy(account);
        
        // IFRS category validation
        validateAccountIFRSCategory(account);
        
        // Format validation
        validateAccountFormat(account);
    }

    public void validateBudgetForCreation(Budget budget) {
        log.debug("Validating budget for creation: {}", budget.getBudgetName());
        
        // Fiscal year validation
        validateBudgetFiscalYear(budget);
        
        // Budget amount validation
        validateBudgetAmounts(budget);
        
        // Budget period validation
        validateBudgetPeriod(budget);
    }

    public void validateYearEndClosing(UUID fiscalYearId) {
        log.debug("Validating year-end closing for fiscal year: {}", fiscalYearId);
        
        // Fiscal year existence and status
        FiscalYear fiscalYear = fiscalYearRepository.findById(fiscalYearId)
            .orElseThrow(() -> new AccountValidationException("Fiscal year not found: " + fiscalYearId));
        
        if (fiscalYear.getIsClosed()) {
            throw new AccountValidationException("Fiscal year is already closed");
        }
        
        // All periods must be closed
        List<AccountingPeriod> openPeriods = accountingPeriodRepository.findOpenPeriodsByFiscalYear(
            fiscalYear.getTenantId(), fiscalYearId);
        if (!openPeriods.isEmpty()) {
            throw new AccountValidationException("Cannot close fiscal year with open periods: " + openPeriods.size());
        }
        
        // Check for pending journals
        List<JournalHeader> pendingJournals = journalHeaderRepository.findByTenantIdAndStatus(
            fiscalYear.getTenantId(), JournalHeader.JournalStatus.PENDING_APPROVAL);
        if (!pendingJournals.isEmpty()) {
            throw new AccountValidationException("Cannot close fiscal year with pending journals: " + pendingJournals.size());
        }
        
        // Validate closing entries
        validateClosingEntries(fiscalYear);
    }

    private void validateJournalBasicRules(JournalHeader journalHeader) {
        if (journalHeader.getJournalLines() == null || journalHeader.getJournalLines().isEmpty()) {
            throw new JournalPostingException("Journal must have at least one line");
        }
        
        if (journalHeader.getJournalDate() == null) {
            throw new JournalPostingException("Journal date is required");
        }
        
        if (journalHeader.getAccountingPeriodId() == null) {
            throw new JournalPostingException("Accounting period is required");
        }
        
        if (journalHeader.getDescription() == null || journalHeader.getDescription().trim().isEmpty()) {
            throw new JournalPostingException("Journal description is required");
        }
        
        if (journalHeader.getJournalDate().isAfter(LocalDate.now())) {
            throw new JournalPostingException("Journal date cannot be in the future");
        }
    }

    private void validateAccountingPeriodForJournal(JournalHeader journalHeader) {
        Optional<AccountingPeriod> periodOpt = accountingPeriodRepository.findById(journalHeader.getAccountingPeriodId());
        if (periodOpt.isEmpty()) {
            throw new JournalPostingException("Accounting period not found: " + journalHeader.getAccountingPeriodId());
        }
        
        AccountingPeriod period = periodOpt.get();
        
        if (!period.getIsOpen() || period.getIsClosed()) {
            throw new JournalPostingException("Accounting period is closed: " + period.getPeriodName());
        }
        
        // Validate journal date is within period
        if (journalHeader.getJournalDate().isBefore(period.getStartDate()) || 
            journalHeader.getJournalDate().isAfter(period.getEndDate())) {
            throw new JournalPostingException("Journal date must be within accounting period: " + period.getPeriodName());
        }
        
        // Check if fiscal year is closed
        Optional<FiscalYear> fiscalYearOpt = fiscalYearRepository.findById(period.getFiscalYearId());
        if (fiscalYearOpt.isPresent() && fiscalYearOpt.get().getIsClosed()) {
            throw new JournalPostingException("Cannot post to closed fiscal year");
        }
    }

    private void validateJournalAccounts(JournalHeader journalHeader) {
        for (JournalLine line : journalHeader.getJournalLines()) {
            Optional<Account> accountOpt = accountRepository.findById(line.getAccountId());
            if (accountOpt.isEmpty()) {
                throw new JournalPostingException("Account not found: " + line.getAccountId());
            }
            
            Account account = accountOpt.get();
            
            if (!account.getIsActive()) {
                throw new JournalPostingException("Account is not active: " + account.getAccountCode());
            }
            
            if (!account.getAllowManualEntry() && journalHeader.getJournalType() == JournalHeader.JournalType.MANUAL) {
                throw new JournalPostingException("Manual entry not allowed for account: " + account.getAccountCode());
            }
            
            // Validate amount
            if ((line.getDebitAmount() == null || line.getDebitAmount().compareTo(BigDecimal.ZERO) <= 0) &&
                (line.getCreditAmount() == null || line.getCreditAmount().compareTo(BigDecimal.ZERO) <= 0)) {
                throw new JournalPostingException("Journal line must have either debit or credit amount greater than zero");
            }
            
            if (line.getDebitAmount() != null && line.getCreditAmount() != null && 
                line.getDebitAmount().compareTo(BigDecimal.ZERO) > 0 && line.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                throw new JournalPostingException("Journal line cannot have both debit and credit amounts");
            }
        }
    }

    private void validateJournalBalance(JournalHeader journalHeader) {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        
        for (JournalLine line : journalHeader.getJournalLines()) {
            if (line.getDebitAmount() != null && line.getDebitAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalDebit = totalDebit.add(line.getDebitAmount());
            }
            if (line.getCreditAmount() != null && line.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalCredit = totalCredit.add(line.getCreditAmount());
            }
        }
        
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new JournalPostingException(
                String.format("Journal is out of balance. Total Debit: %s, Total Credit: %s, Difference: %s", 
                             totalDebit, totalCredit, totalDebit.subtract(totalCredit)));
        }
        
        // Update totals
        journalHeader.setTotalDebit(totalDebit);
        journalHeader.setTotalCredit(totalCredit);
    }

    private void validateJournalBusinessRules(JournalHeader journalHeader) {
        // Validate no duplicate accounts in journal
        long uniqueAccounts = journalHeader.getJournalLines().stream()
            .map(JournalLine::getAccountId)
            .distinct()
            .count();
        
        if (uniqueAccounts != journalHeader.getJournalLines().size()) {
            throw new JournalPostingException("Journal contains duplicate accounts");
        }
        
        // Validate reference number uniqueness for certain journal types
        if (journalHeader.getReferenceNumber() != null && 
            (journalHeader.getJournalType() == JournalHeader.JournalType.MANUAL || 
             journalHeader.getJournalType() == JournalHeader.JournalType.SYSTEM)) {
            
            boolean exists = journalHeaderRepository.existsByTenantIdAndJournalNumber(
                journalHeader.getTenantId(), journalHeader.getReferenceNumber());
            if (exists) {
                throw new JournalPostingException("Reference number already exists: " + journalHeader.getReferenceNumber());
            }
        }
        
        // Validate reversal journals
        if (journalHeader.getJournalType() == JournalHeader.JournalType.REVERSAL && 
            journalHeader.getOriginalJournalId() == null) {
            throw new JournalPostingException("Reversal journal must reference original journal");
        }
    }

    private void validateJournalAgainstBudget(JournalHeader journalHeader) {
        // Check budget constraints for expense accounts
        for (JournalLine line : journalHeader.getJournalLines()) {
            Optional<Account> accountOpt = accountRepository.findById(line.getAccountId());
            if (accountOpt.isPresent()) {
                Account account = accountOpt.get();
                
                // Check budget for expense accounts
                if (account.getAccountType() == Account.AccountType.EXPENSE && line.getDebitAmount() != null) {
                    List<BudgetLine> budgetLines = budgetLineRepository.findByTenantIdAndAccountId(
                        journalHeader.getTenantId(), line.getAccountId());
                    
                    for (BudgetLine budgetLine : budgetLines) {
                        if (budgetLine.getAvailableAmount() != null && 
                            line.getDebitAmount().compareTo(budgetLine.getAvailableAmount()) > 0) {
                            
                            // Only warn for budget overruns, don't block
                            log.warn("Budget warning: Journal line amount {} exceeds available budget {} for account {}", 
                                    line.getDebitAmount(), budgetLine.getAvailableAmount(), account.getAccountCode());
                        }
                    }
                }
            }
        }
    }

    private void validateAccountBasicRules(Account account) {
        if (account.getAccountCode() == null || account.getAccountCode().trim().isEmpty()) {
            throw new AccountValidationException("Account code is required");
        }
        
        if (account.getAccountName() == null || account.getAccountName().trim().isEmpty()) {
            throw new AccountValidationException("Account name is required");
        }
        
        if (account.getAccountType() == null) {
            throw new AccountValidationException("Account type is required");
        }
        
        if (account.getNormalBalance() == null) {
            throw new AccountValidationException("Normal balance is required");
        }
        
        // Validate account code format
        if (!account.getAccountCode().matches("^[0-9]+$")) {
            throw new AccountValidationException("Account code must contain only numbers");
        }
        
        if (account.getAccountCode().length() < 3 || account.getAccountCode().length() > 20) {
            throw new AccountValidationException("Account code must be between 3 and 20 characters");
        }
        
        // Validate uniqueness
        if (accountRepository.existsByTenantIdAndAccountCode(account.getTenantId(), account.getAccountCode())) {
            throw new AccountValidationException("Account code already exists: " + account.getAccountCode());
        }
    }

    private void validateAccountHierarchy(Account account) {
        if (account.getParentAccountId() != null) {
            Optional<Account> parentAccountOpt = accountRepository.findById(account.getParentAccountId());
            if (parentAccountOpt.isEmpty()) {
                throw new AccountValidationException("Parent account not found: " + account.getParentAccountId());
            }
            
            Account parentAccount = parentAccountOpt.get();
            
            // Validate parent is active
            if (!parentAccount.getIsActive()) {
                throw new AccountValidationException("Parent account is not active: " + parentAccount.getAccountCode());
            }
            
            // Validate no circular reference
            validateNoCircularReference(account.getId(), account.getParentAccountId());
            
            // Validate account type compatibility
            if (!isAccountTypeCompatible(account.getAccountType(), parentAccount.getAccountType())) {
                throw new AccountValidationException("Account type not compatible with parent account type");
            }
        }
    }

    private void validateAccountIFRSCategory(Account account) {
        if (account.getIFRSCategory() != null) {
            // Validate IFRS category matches account type
            switch (account.getAccountType()) {
                case ASSET:
                    if (account.getIFRSCategory() != Account.IFRSCategory.CURRENT_ASSETS && 
                        account.getIFRSCategory() != Account.IFRSCategory.NON_CURRENT_ASSETS) {
                        throw new AccountValidationException("Asset accounts must have CURRENT_ASSETS or NON_CURRENT_ASSETS IFRS category");
                    }
                    break;
                case LIABILITY:
                    if (account.getIFRSCategory() != Account.IFRSCategory.CURRENT_LIABILITIES && 
                        account.getIFRSCategory() != Account.IFRSCategory.NON_CURRENT_LIABILITIES) {
                        throw new AccountValidationException("Liability accounts must have CURRENT_LIABILITIES or NON_CURRENT_LIABILITIES IFRS category");
                    }
                    break;
                case EQUITY:
                    if (account.getIFRSCategory() != Account.IFRSCategory.EQUITY) {
                        throw new AccountValidationException("Equity accounts must have EQUITY IFRS category");
                    }
                    break;
                case REVENUE:
                    if (account.getIFRSCategory() != Account.IFRSCategory.REVENUE && 
                        account.getIFRSCategory() != Account.IFRSCategory.OTHER_INCOME) {
                        throw new AccountValidationException("Revenue accounts must have REVENUE or OTHER_INCOME IFRS category");
                    }
                    break;
                case EXPENSE:
                case GAIN:
                case LOSS:
                    if (account.getIFRSCategory() != Account.IFRSCategory.OPERATING_EXPENSES && 
                        account.getIFRSCategory() != Account.IFRSCategory.OTHER_EXPENSES) {
                        throw new AccountValidationException("Expense accounts must have OPERATING_EXPENSES or OTHER_EXPENSES IFRS category");
                    }
                    break;
            }
        }
    }

    private void validateAccountFormat(Account account) {
        if (account.getAccountFormat() != null) {
            // Validate account format pattern
            if (!account.getAccountFormat().matches("^[0-9\\-\\*]+$")) {
                throw new AccountValidationException("Account format can only contain numbers, hyphens, and asterisks");
            }
        }
        
        if (account.getMinLength() != null && account.getMaxLength() != null) {
            if (account.getMinLength() > account.getMaxLength()) {
                throw new AccountValidationException("Minimum length cannot be greater than maximum length");
            }
        }
    }

    private void validateBudgetFiscalYear(Budget budget) {
        Optional<FiscalYear> fiscalYearOpt = fiscalYearRepository.findById(budget.getFiscalYearId());
        if (fiscalYearOpt.isEmpty()) {
            throw new BudgetValidationException("Fiscal year not found: " + budget.getFiscalYearId());
        }
        
        FiscalYear fiscalYear = fiscalYearOpt.get();
        
        if (fiscalYear.getIsClosed()) {
            throw new BudgetValidationException("Cannot create budget for closed fiscal year");
        }
        
        // Validate budget uniqueness
        if (budgetRepository.existsByTenantIdAndFiscalYearIdAndBudgetName(
                budget.getTenantId(), budget.getFiscalYearId(), budget.getBudgetName())) {
            throw new BudgetValidationException("Budget name already exists for this fiscal year");
        }
    }

    private void validateBudgetAmounts(Budget budget) {
        if (budget.getTotalBudgetAmount() != null && budget.getTotalBudgetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BudgetValidationException("Total budget amount must be greater than zero");
        }
    }

    private void validateBudgetPeriod(Budget budget) {
        if (budget.getEffectiveFrom() != null && budget.getEffectiveTo() != null) {
            if (budget.getEffectiveFrom().isAfter(budget.getEffectiveTo())) {
                throw new BudgetValidationException("Effective from date cannot be after effective to date");
            }
        }
    }

    private void validateClosingEntries(FiscalYear fiscalYear) {
        // Validate that all revenue and expense accounts have been closed
        // This would involve checking for closing journals
        log.debug("Validating closing entries for fiscal year: {}", fiscalYear.getYearNumber());
    }

    private void validateNoCircularReference(UUID accountId, UUID parentAccountId) {
        UUID currentParentId = parentAccountId;
        int maxDepth = 10;
        
        while (currentParentId != null && maxDepth-- > 0) {
            if (currentParentId.equals(accountId)) {
                throw new AccountValidationException("Circular reference detected in account hierarchy");
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

    private boolean isAccountTypeCompatible(Account.AccountType childType, Account.AccountType parentType) {
        // Parent accounts can be of any type, but child accounts should be compatible
        // This is a simplified validation - in reality, this might be more complex
        return true;
    }

    public void validateJournalReversal(JournalHeader originalJournal, String reversalReason) {
        if (originalJournal.getStatus() != JournalHeader.JournalStatus.POSTED) {
            throw new JournalPostingException("Only posted journals can be reversed");
        }
        
        if (originalJournal.getIsReversed()) {
            throw new JournalPostingException("Journal has already been reversed");
        }
        
        if (reversalReason == null || reversalReason.trim().isEmpty()) {
            throw new JournalPostingException("Reversal reason is required");
        }
        
        // Validate reversal timing (e.g., within same fiscal year)
        LocalDate reversalDate = LocalDate.now();
        Optional<FiscalYear> originalFiscalYearOpt = fiscalYearRepository.findFiscalYearForDate(
            originalJournal.getTenantId(), originalJournal.getJournalDate());
        Optional<FiscalYear> reversalFiscalYearOpt = fiscalYearRepository.findFiscalYearForDate(
            originalJournal.getTenantId(), reversalDate);
        
        if (originalFiscalYearOpt.isPresent() && reversalFiscalYearOpt.isPresent() &&
            !originalFiscalYearOpt.get().getId().equals(reversalFiscalYearOpt.get().getId())) {
            // Allow cross-fiscal-year reversals but with warning
            log.warn("Reversing journal across fiscal years: {} -> {}", 
                    originalFiscalYearOpt.get().getYearNumber(), reversalFiscalYearOpt.get().getYearNumber());
        }
    }
}
