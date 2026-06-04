package com.financial.corefinance.service;

import com.financial.corefinance.domain.entity.*;
import com.financial.corefinance.dto.response.YearEndCloseResult;
import com.financial.corefinance.exception.AccountValidationException;
import com.financial.corefinance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class YearEndClosingService {

    private final FiscalYearRepository fiscalYearRepository;
    private final AccountingPeriodRepository accountingPeriodRepository;
    private final AccountRepository accountRepository;
    private final JournalHeaderRepository journalHeaderRepository;
    private final JournalLineRepository journalLineRepository;
    private final TenantAccountingSettingsRepository settingsRepository;
    private final NumberingSeriesService numberingSeriesService;
    private final GlReportingService glReportingService;

    private static final Set<Account.AccountType> NOMINAL_TYPES = EnumSet.of(
            Account.AccountType.REVENUE,
            Account.AccountType.EXPENSE,
            Account.AccountType.GAIN,
            Account.AccountType.LOSS
    );

    private boolean isNominalAccount(Account account) {
        if (NOMINAL_TYPES.contains(account.getAccountType())) {
            return true;
        }
        Account.IFRSCategory ifrs = account.getIFRSCategory();
        if (ifrs == null) {
            return false;
        }
        return ifrs == Account.IFRSCategory.REVENUE
                || ifrs == Account.IFRSCategory.OTHER_INCOME
                || ifrs == Account.IFRSCategory.OPERATING_EXPENSES
                || ifrs == Account.IFRSCategory.OTHER_EXPENSES;
    }

    /**
     * Execute the year-end closing process for a fiscal year.
     */
    @Transactional
    public YearEndCloseResult closeFiscalYear(UUID fiscalYearId, String closedBy) {
        log.info("Starting year-end close for fiscal year: {}", fiscalYearId);

        FiscalYear fy = fiscalYearRepository.findById(fiscalYearId)
                .orElseThrow(() -> new AccountValidationException("Fiscal year not found: " + fiscalYearId));

        if (fy.getIsClosed()) {
            throw new AccountValidationException("Fiscal year is already closed.");
        }

        String tenantId = fy.getTenantId();

        // Prevent duplicate close
        List<JournalHeader> existingClosing = journalHeaderRepository
                .findByTenantIdAndJournalType(tenantId, JournalHeader.JournalType.CLOSING);
        for (JournalHeader existing : existingClosing) {
            if (existing.getAccountingPeriod() != null &&
                existing.getAccountingPeriod().getFiscalYearId().equals(fiscalYearId) &&
                existing.getStatus() == JournalHeader.JournalStatus.POSTED &&
                (existing.getIsReversed() == null || !existing.getIsReversed())) {
                throw new AccountValidationException("Year-end close has already been run for this fiscal year.");
            }
        }

        // Validate all periods are closed
        List<AccountingPeriod> openPeriods = accountingPeriodRepository
                .findOpenPeriodsByFiscalYear(tenantId, fiscalYearId);
        if (!openPeriods.isEmpty()) {
            throw new AccountValidationException(
                    "Cannot close year: " + openPeriods.size() + " period(s) are still open.");
        }

        // Get retained earnings account
        TenantAccountingSettings settings = settingsRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new AccountValidationException(
                        "Tenant accounting settings not configured. Please set the Retained Earnings account code in Settings."));

        if (settings.getRetainedEarningsAccountCode() == null || settings.getRetainedEarningsAccountCode().isBlank()) {
            throw new AccountValidationException(
                    "Retained Earnings account code not configured in Settings.");
        }

        Account retainedEarnings = accountRepository
                .findByTenantIdAndAccountCode(tenantId, settings.getRetainedEarningsAccountCode())
                .orElseThrow(() -> new AccountValidationException(
                        "Retained Earnings account not found: " + settings.getRetainedEarningsAccountCode()));

        // Get last period for housing the closing entry
        List<AccountingPeriod> periods = accountingPeriodRepository
                .findByTenantIdAndFiscalYearIdOrderByPeriodNumber(tenantId, fiscalYearId);
        if (periods.isEmpty()) {
            throw new AccountValidationException("Fiscal year has no periods.");
        }
        AccountingPeriod lastPeriod = periods.get(periods.size() - 1);

        // Temporarily reopen last period for the closing entry
        boolean wasLastPeriodClosed = lastPeriod.getIsClosed();
        if (wasLastPeriodClosed) {
            lastPeriod.setIsClosed(false);
            lastPeriod.setIsOpen(true);
            accountingPeriodRepository.save(lastPeriod);
        }

        try {
            List<Account> nominalAccounts = accountRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                    .filter(this::isNominalAccount)
                    .toList();

            BigDecimal totalRevenueCredits = BigDecimal.ZERO;
            BigDecimal totalExpenseDebits = BigDecimal.ZERO;
            List<JournalLine> closingLines = new ArrayList<>();
            int lineNumber = 1;

            for (Account account : nominalAccounts) {
                // Close full current balance (opening + all posted activity), not FY journals only
                BigDecimal currentBalance = glReportingService.calculateCurrentBalance(tenantId, account.getId());
                if (currentBalance.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }

                JournalLine closingLine = JournalLine.builder()
                        .tenantId(tenantId)
                        .lineNumber(lineNumber++)
                        .accountId(account.getId())
                        .description("Year-end close: " + account.getAccountCode() + " " + account.getAccountName())
                        .referenceType("YEAR_END_CLOSE")
                        .currencyCode(account.getCurrencyCode())
                        .build();

                if (account.getNormalBalance() == Account.NormalBalance.CREDIT) {
                    if (currentBalance.compareTo(BigDecimal.ZERO) > 0) {
                        closingLine.setDebitAmount(currentBalance);
                        closingLine.setCreditAmount(BigDecimal.ZERO);
                        totalRevenueCredits = totalRevenueCredits.add(currentBalance);
                    } else {
                        closingLine.setCreditAmount(currentBalance.abs());
                        closingLine.setDebitAmount(BigDecimal.ZERO);
                        totalRevenueCredits = totalRevenueCredits.subtract(currentBalance.abs());
                    }
                } else {
                    if (currentBalance.compareTo(BigDecimal.ZERO) > 0) {
                        closingLine.setCreditAmount(currentBalance);
                        closingLine.setDebitAmount(BigDecimal.ZERO);
                        totalExpenseDebits = totalExpenseDebits.add(currentBalance);
                    } else {
                        closingLine.setDebitAmount(currentBalance.abs());
                        closingLine.setCreditAmount(BigDecimal.ZERO);
                        totalExpenseDebits = totalExpenseDebits.subtract(currentBalance.abs());
                    }
                }
                closingLines.add(closingLine);
            }

            BigDecimal netIncome = totalRevenueCredits.subtract(totalExpenseDebits);

            if (netIncome.compareTo(BigDecimal.ZERO) != 0) {
                JournalLine reLine = JournalLine.builder()
                        .tenantId(tenantId)
                        .lineNumber(lineNumber)
                        .accountId(retainedEarnings.getId())
                        .description("Year-end close: Net income to Retained Earnings")
                        .referenceType("YEAR_END_CLOSE")
                        .currencyCode(retainedEarnings.getCurrencyCode())
                        .build();
                if (netIncome.compareTo(BigDecimal.ZERO) > 0) {
                    reLine.setCreditAmount(netIncome);
                    reLine.setDebitAmount(BigDecimal.ZERO);
                } else {
                    reLine.setDebitAmount(netIncome.abs());
                    reLine.setCreditAmount(BigDecimal.ZERO);
                }
                closingLines.add(reLine);
            }

            int nominalClosedCount = (int) closingLines.stream()
                    .filter(l -> !retainedEarnings.getId().equals(l.getAccountId()))
                    .count();

            if (closingLines.isEmpty()) {
                fy.setIsClosed(true);
                fy.setClosedAt(LocalDate.now());
                fy.setClosedBy(closedBy);
                fiscalYearRepository.save(fy);
                log.info("Year-end close: fiscal year marked closed with no P&L balances in year {}", fy.getYearName());
                return YearEndCloseResult.builder()
                        .closingJournal(null)
                        .nominalAccountsClosed(0)
                        .netIncome(BigDecimal.ZERO)
                        .fiscalYearMarkedClosed(true)
                        .build();
            }

            String journalNumber;
            try {
                journalNumber = numberingSeriesService.getNextNumber("JOURNAL").get("nextNumber");
            } catch (Exception e) {
                journalNumber = "CLO-" + fy.getYearNumber() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            }

            JournalHeader closingJournal = JournalHeader.builder()
                    .tenantId(tenantId)
                    .journalNumber(journalNumber)
                    .journalDate(fy.getEndDate())
                    .accountingPeriodId(lastPeriod.getId())
                    .journalType(JournalHeader.JournalType.CLOSING)
                    .description("Automated year-end closing for " + fy.getYearName())
                    .sourceSystem("YEAR_END_CLOSE")
                    .status(JournalHeader.JournalStatus.POSTED)
                    .postedAt(LocalDateTime.now())
                    .postedBy(closedBy)
                    .build();

            JournalHeader savedHeader = journalHeaderRepository.save(closingJournal);

            for (JournalLine line : closingLines) {
                line.setJournalHeaderId(savedHeader.getId());
            }
            journalLineRepository.saveAll(closingLines);

            BigDecimal totalDebit = closingLines.stream()
                    .map(l -> l.getDebitAmount() != null ? l.getDebitAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalCredit = closingLines.stream()
                    .map(l -> l.getCreditAmount() != null ? l.getCreditAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            savedHeader.setTotalDebit(totalDebit);
            savedHeader.setTotalCredit(totalCredit);
            journalHeaderRepository.save(savedHeader);

            // Clear opening balances on closed P&L accounts so current balance stays at zero
            Set<UUID> closedNominalIds = new HashSet<>();
            for (JournalLine line : closingLines) {
                if (!retainedEarnings.getId().equals(line.getAccountId())) {
                    closedNominalIds.add(line.getAccountId());
                }
            }
            for (UUID accountId : closedNominalIds) {
                accountRepository.findById(accountId).ifPresent(nominal -> {
                    if (nominal.getOpeningBalance() != null
                            && nominal.getOpeningBalance().compareTo(BigDecimal.ZERO) != 0) {
                        nominal.setOpeningBalance(BigDecimal.ZERO);
                        accountRepository.save(nominal);
                    }
                });
            }

            fy.setIsClosed(true);
            fy.setClosedAt(LocalDate.now());
            fy.setClosedBy(closedBy);
            fiscalYearRepository.save(fy);

            log.info("Year-end close completed. Net income: {}. Journal: {}", netIncome, savedHeader.getJournalNumber());
            return YearEndCloseResult.builder()
                    .closingJournal(savedHeader)
                    .nominalAccountsClosed(nominalClosedCount)
                    .netIncome(netIncome)
                    .fiscalYearMarkedClosed(true)
                    .build();

        } finally {
            if (wasLastPeriodClosed) {
                lastPeriod.setIsClosed(true);
                lastPeriod.setIsOpen(false);
                accountingPeriodRepository.save(lastPeriod);
            }
        }
    }

    /**
     * Reopen a closed fiscal year by reversing the closing journal entries.
     */
    @Transactional
    public JournalHeader reopenFiscalYear(UUID fiscalYearId, String reopenedBy) {
        log.info("Reopening fiscal year: {}", fiscalYearId);

        FiscalYear fy = fiscalYearRepository.findById(fiscalYearId)
                .orElseThrow(() -> new AccountValidationException("Fiscal year not found: " + fiscalYearId));

        if (!fy.getIsClosed()) {
            throw new AccountValidationException("Fiscal year is not closed.");
        }

        String tenantId = fy.getTenantId();

        // Find the closing journal for this year
        JournalHeader closingJournal = null;
        List<JournalHeader> closingJournals = journalHeaderRepository
                .findByTenantIdAndJournalType(tenantId, JournalHeader.JournalType.CLOSING);
        for (JournalHeader cj : closingJournals) {
            if (cj.getAccountingPeriod() != null &&
                cj.getAccountingPeriod().getFiscalYearId().equals(fiscalYearId) &&
                cj.getStatus() == JournalHeader.JournalStatus.POSTED &&
                (cj.getIsReversed() == null || !cj.getIsReversed())) {
                closingJournal = cj;
                break;
            }
        }

        JournalHeader reversalJournal = null;

        if (closingJournal != null) {
            AccountingPeriod period = accountingPeriodRepository.findById(closingJournal.getAccountingPeriodId())
                    .orElseThrow(() -> new AccountValidationException("Period not found"));
            boolean wasClosed = period.getIsClosed();
            if (wasClosed) {
                period.setIsClosed(false);
                period.setIsOpen(true);
                accountingPeriodRepository.save(period);
            }

            try {
                List<JournalLine> originalLines = journalLineRepository.findByTenantIdAndJournalHeaderId(
                        tenantId, closingJournal.getId());

                String reversalNumber;
                try {
                    reversalNumber = numberingSeriesService.getNextNumber("JOURNAL").get("nextNumber");
                } catch (Exception e) {
                    reversalNumber = "REV-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                }

                reversalJournal = JournalHeader.builder()
                        .tenantId(tenantId)
                        .journalNumber(reversalNumber)
                        .journalDate(LocalDate.now())
                        .accountingPeriodId(closingJournal.getAccountingPeriodId())
                        .journalType(JournalHeader.JournalType.REVERSAL)
                        .description("Reversal of year-end closing for " + fy.getYearName())
                        .sourceSystem("YEAR_END_REOPEN")
                        .status(JournalHeader.JournalStatus.POSTED)
                        .originalJournalId(closingJournal.getId())
                        .postedAt(LocalDateTime.now())
                        .postedBy(reopenedBy)
                        .build();

                JournalHeader savedReversal = journalHeaderRepository.save(reversalJournal);

                List<JournalLine> reversalLines = new ArrayList<>();
                int lineNum = 1;
                for (JournalLine original : originalLines) {
                    JournalLine reversal = JournalLine.builder()
                            .tenantId(tenantId)
                            .journalHeaderId(savedReversal.getId())
                            .lineNumber(lineNum++)
                            .accountId(original.getAccountId())
                            .debitAmount(original.getCreditAmount() != null ? original.getCreditAmount() : BigDecimal.ZERO)
                            .creditAmount(original.getDebitAmount() != null ? original.getDebitAmount() : BigDecimal.ZERO)
                            .description("Reversal: " + (original.getDescription() != null ? original.getDescription() : ""))
                            .referenceType("YEAR_END_REVERSAL")
                            .currencyCode(original.getCurrencyCode())
                            .build();
                    reversalLines.add(reversal);
                }
                journalLineRepository.saveAll(reversalLines);

                BigDecimal totalDebit = reversalLines.stream()
                        .map(l -> l.getDebitAmount() != null ? l.getDebitAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalCredit = reversalLines.stream()
                        .map(l -> l.getCreditAmount() != null ? l.getCreditAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                savedReversal.setTotalDebit(totalDebit);
                savedReversal.setTotalCredit(totalCredit);
                journalHeaderRepository.save(savedReversal);

                closingJournal.setIsReversed(true);
                closingJournal.setReversedBy(reopenedBy);
                closingJournal.setReversedAt(LocalDateTime.now());
                closingJournal.setReversalReason("Fiscal year reopened");
                journalHeaderRepository.save(closingJournal);

                reversalJournal = savedReversal;
            } finally {
                if (wasClosed) {
                    period.setIsClosed(true);
                    period.setIsOpen(false);
                    accountingPeriodRepository.save(period);
                }
            }
        }

        fy.setIsClosed(false);
        fy.setClosedAt(null);
        fy.setClosedBy(null);
        fiscalYearRepository.save(fy);

        log.info("Fiscal year {} reopened.", fy.getYearName());
        return reversalJournal;
    }
}
