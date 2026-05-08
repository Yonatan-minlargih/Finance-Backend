package com.financial.corefinance.service;

import com.financial.corefinance.domain.entity.*;
import com.financial.corefinance.exception.JournalPostingException;
import com.financial.corefinance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class YearEndClosingService {

    private final FiscalYearRepository fiscalYearRepository;
    private final AccountingPeriodRepository accountingPeriodRepository;
    private final AccountRepository accountRepository;
    private final JournalHeaderRepository journalHeaderRepository;
    private final JournalLineRepository journalLineRepository;
    private final PostingEngineService postingEngineService;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public JournalHeader performYearEndClosing(UUID fiscalYearId, String closedBy) {
        log.info("Performing year-end closing for fiscal year: {}", fiscalYearId);
        
        // Validate fiscal year exists and is not already closed
        FiscalYear fiscalYear = fiscalYearRepository.findById(fiscalYearId)
            .orElseThrow(() -> new JournalPostingException("Fiscal year not found: " + fiscalYearId));
        
        if (fiscalYear.getIsClosed()) {
            throw new JournalPostingException("Fiscal year is already closed");
        }
        
        // Validate all periods are closed
        List<AccountingPeriod> openPeriods = accountingPeriodRepository.findOpenPeriodsByFiscalYear(
            fiscalYear.getTenantId(), fiscalYearId);
        if (!openPeriods.isEmpty()) {
            throw new JournalPostingException("Cannot close fiscal year with open periods");
        }
        
        // Get closing period (should be the last period)
        List<AccountingPeriod> periods = accountingPeriodRepository.findByTenantIdAndFiscalYearIdOrderByPeriodNumber(
            fiscalYear.getTenantId(), fiscalYearId);
        if (periods.isEmpty()) {
            throw new JournalPostingException("No accounting periods found for fiscal year");
        }
        
        AccountingPeriod closingPeriod = periods.get(periods.size() - 1);
        
        // Create year-end closing journal
        JournalHeader closingJournal = createYearEndClosingJournal(fiscalYear, closingPeriod, closedBy);
        
        // Post the closing journal
        JournalHeader postedJournal = postingEngineService.createAndPostJournal(closingJournal);
        
        // Close the fiscal year
        fiscalYear.setIsClosed(true);
        fiscalYear.setClosedAt(LocalDate.now());
        fiscalYear.setClosedBy(closedBy);
        fiscalYearRepository.save(fiscalYear);
        
        // Create audit log
        createAuditLog(fiscalYear, "Year-end closing completed", closedBy);
        
        log.info("Year-end closing completed successfully for fiscal year: {}", fiscalYear.getYearNumber());
        return postedJournal;
    }

    @Transactional
    public JournalHeader reverseYearEndClosing(UUID originalClosingJournalId, String reversedBy) {
        log.info("Reversing year-end closing journal: {}", originalClosingJournalId);
        
        // Validate original closing journal exists and is posted
        JournalHeader originalJournal = journalHeaderRepository.findById(originalClosingJournalId)
            .orElseThrow(() -> new JournalPostingException("Original closing journal not found"));
        
        if (originalJournal.getStatus() != JournalHeader.JournalStatus.POSTED) {
            throw new JournalPostingException("Original journal must be posted to reverse");
        }
        
        if (originalJournal.getJournalType() != JournalHeader.JournalType.CLOSING) {
            throw new JournalPostingException("Only closing journals can be reversed");
        }
        
        // Create reversal journal
        JournalHeader reversalJournal = postingEngineService.reverseJournal(originalClosingJournalId, 
            "Year-end closing reversal - " + LocalDate.now());
        
        // Reopen the fiscal year
        FiscalYear fiscalYear = fiscalYearRepository.findFiscalYearForDate(
                originalJournal.getTenantId(), originalJournal.getJournalDate())
            .orElseThrow(() -> new JournalPostingException("Fiscal year not found"));
        
        fiscalYear.setIsClosed(false);
        fiscalYearRepository.save(fiscalYear);
        
        // Create audit log
        createAuditLog(fiscalYear, "Year-end closing reversed", reversedBy);
        
        log.info("Year-end closing reversed successfully for fiscal year: {}", fiscalYear.getYearNumber());
        return reversalJournal;
    }

    private JournalHeader createYearEndClosingJournal(FiscalYear fiscalYear, AccountingPeriod closingPeriod, String closedBy) {
        log.info("Creating year-end closing journal for fiscal year: {}", fiscalYear.getYearNumber());
        
        String tenantId = fiscalYear.getTenantId();
        
        // Get revenue and expense accounts to close
        List<Account> revenueAccounts = accountRepository.findByTenantIdAndAccountType(tenantId, Account.AccountType.REVENUE);
        List<Account> expenseAccounts = accountRepository.findByTenantIdAndAccountType(tenantId, Account.AccountType.EXPENSE);
        
        // Get retained earnings account (should be an equity account)
        Account retainedEarningsAccount = findRetainedEarningsAccount(tenantId);
        
        JournalHeader closingJournal = JournalHeader.builder()
            .tenantId(tenantId)
            .journalDate(closingPeriod.getEndDate())
            .accountingPeriodId(closingPeriod.getId())
            .journalType(JournalHeader.JournalType.CLOSING)
            .description("Year-End Closing - Fiscal Year " + fiscalYear.getYearNumber())
            .narration("Closing revenue and expense accounts to retained earnings")
            .createdBy(closedBy)
            .build();
        
        // Close revenue accounts (debit revenue, credit retained earnings)
        for (Account revenueAccount : revenueAccounts) {
            if (revenueAccount.getIsActive()) {
                BigDecimal revenueBalance = calculateAccountBalance(revenueAccount.getId(), fiscalYear.getId());
                if (revenueBalance.compareTo(BigDecimal.ZERO) > 0) {
                    JournalLine revenueClosingLine = JournalLine.builder()
                        .tenantId(tenantId)
                        .accountId(revenueAccount.getId())
                        .debitAmount(revenueBalance)
                        .description("Close " + revenueAccount.getAccountCode() + " to retained earnings")
                        .build();
                    
                    closingJournal.getJournalLines().add(revenueClosingLine);
                    
                    // Credit retained earnings
                    JournalLine earningsCreditLine = JournalLine.builder()
                        .tenantId(tenantId)
                        .accountId(retainedEarningsAccount.getId())
                        .creditAmount(revenueBalance)
                        .description("Revenue from " + revenueAccount.getAccountCode())
                        .build();
                    
                    closingJournal.getJournalLines().add(earningsCreditLine);
                }
            }
        }
        
        // Close expense accounts (credit expense, debit retained earnings)
        for (Account expenseAccount : expenseAccounts) {
            if (expenseAccount.getIsActive()) {
                BigDecimal expenseBalance = calculateAccountBalance(expenseAccount.getId(), fiscalYear.getId());
                if (expenseBalance.compareTo(BigDecimal.ZERO) > 0) {
                    JournalLine expenseClosingLine = JournalLine.builder()
                        .tenantId(tenantId)
                        .accountId(expenseAccount.getId())
                        .creditAmount(expenseBalance)
                        .description("Close " + expenseAccount.getAccountCode() + " to retained earnings")
                        .build();
                    
                    closingJournal.getJournalLines().add(expenseClosingLine);
                    
                    // Debit retained earnings
                    JournalLine earningsDebitLine = JournalLine.builder()
                        .tenantId(tenantId)
                        .accountId(retainedEarningsAccount.getId())
                        .debitAmount(expenseBalance)
                        .description("Expense from " + expenseAccount.getAccountCode())
                        .build();
                    
                    closingJournal.getJournalLines().add(earningsDebitLine);
                }
            }
        }
        
        if (closingJournal.getJournalLines().isEmpty()) {
            throw new JournalPostingException("No balances to close for fiscal year: " + fiscalYear.getYearNumber());
        }
        
        return closingJournal;
    }

    private Account findRetainedEarningsAccount(String tenantId) {
        List<Account> equityAccounts = accountRepository.findByTenantIdAndAccountType(tenantId, Account.AccountType.EQUITY);
        
        // Look for account with "retained earnings" in the name or code
        for (Account account : equityAccounts) {
            if ((account.getAccountCode().toLowerCase().contains("retained") || 
                 account.getAccountName().toLowerCase().contains("retained")) &&
                (account.getAccountCode().toLowerCase().contains("earnings") || 
                 account.getAccountName().toLowerCase().contains("earnings"))) {
                return account;
            }
        }
        
        // If not found, create one
        Account retainedEarningsAccount = Account.builder()
            .tenantId(tenantId)
            .accountCode("300000")
            .accountName("Retained Earnings")
            .accountType(Account.AccountType.EQUITY)
            .normalBalance(Account.NormalBalance.CREDIT)
            .ifrsCategory(Account.IFRSCategory.EQUITY)
            .isActive(true)
            .allowManualEntry(false) // Typically not allowed for retained earnings
            .currencyCode("USD")
            .build();
        
        return accountRepository.save(retainedEarningsAccount);
    }

    private BigDecimal calculateAccountBalance(UUID accountId, UUID fiscalYearId) {
        // Calculate the year-to-date balance for an account within the fiscal year
        // This would sum all journal lines for the account in all periods of the fiscal year
        
        log.debug("Calculating balance for account: {} in fiscal year: {}", accountId, fiscalYearId);
        
        // Get all accounting periods for the fiscal year
        List<AccountingPeriod> periods = accountingPeriodRepository.findByTenantIdAndFiscalYearIdOrderByPeriodNumber(
            com.financial.corefinance.domain.base.TenantContext.getCurrentTenant(), fiscalYearId);
        
        BigDecimal totalBalance = BigDecimal.ZERO;
        
        for (AccountingPeriod period : periods) {
            // Get posted journal lines for this account in this period
            List<JournalLine> journalLines = journalLineRepository.findPostedLinesByAccount(
                com.financial.corefinance.domain.base.TenantContext.getCurrentTenant(), accountId);
            
            for (JournalLine line : journalLines) {
                // Check if the journal is in this period
                Optional<JournalHeader> journalHeader = journalHeaderRepository.findById(line.getJournalHeaderId());
                if (journalHeader.isPresent() && journalHeader.get().getAccountingPeriodId().equals(period.getId())) {
                    if (line.getDebitAmount() != null) {
                        totalBalance = totalBalance.add(line.getDebitAmount());
                    }
                    if (line.getCreditAmount() != null) {
                        totalBalance = totalBalance.subtract(line.getCreditAmount());
                    }
                }
            }
        }
        
        return totalBalance.abs(); // Return absolute value for closing purposes
    }

    @Transactional(readOnly = true)
    public boolean canCloseFiscalYear(UUID fiscalYearId) {
        FiscalYear fiscalYear = fiscalYearRepository.findById(fiscalYearId).orElse(null);
        if (fiscalYear == null || fiscalYear.getIsClosed()) {
            return false;
        }
        
        // Check if all periods are closed
        long openPeriodCount = accountingPeriodRepository.countOpenPeriodsByFiscalYear(
            fiscalYear.getTenantId(), fiscalYearId);
        
        return openPeriodCount == 0;
    }

    @Transactional(readOnly = true)
    public List<AccountingPeriod> getOpenPeriods(UUID fiscalYearId) {
        FiscalYear fiscalYear = fiscalYearRepository.findById(fiscalYearId).orElse(null);
        if (fiscalYear == null) {
            return List.of();
        }
        
        return accountingPeriodRepository.findOpenPeriodsByFiscalYear(fiscalYear.getTenantId(), fiscalYearId);
    }

    private void createAuditLog(FiscalYear fiscalYear, String action, String performedBy) {
        AuditLog auditLog = AuditLog.builder()
            .tenantId(fiscalYear.getTenantId())
            .entityType("FiscalYear")
            .entityId(fiscalYear.getId())
            .action(AuditLog.AuditAction.CREATE)
            .newValue("Fiscal Year: " + fiscalYear.getYearNumber() + ", Action: " + action)
            .userId(performedBy)
            .additionalInfo("Year-end closing operation")
            .moduleName("YearEndClosing")
            .functionName("performYearEndClosing")
            .businessTransactionId("FY-" + fiscalYear.getYearNumber())
            .build();
        
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<JournalHeader> getClosingJournals(UUID fiscalYearId) {
        return journalHeaderRepository.findByTenantIdAndJournalTypeAndFiscalYearId(
            com.financial.corefinance.domain.base.TenantContext.getCurrentTenant(),
            JournalHeader.JournalType.CLOSING,
            fiscalYearId);
    }
}
