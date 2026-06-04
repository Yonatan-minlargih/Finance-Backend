package com.financial.corefinance.service;

import com.financial.corefinance.domain.entity.Account;
import com.financial.corefinance.repository.AccountRepository;
import com.financial.corefinance.repository.JournalLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GlReportingService {

    private final JournalLineRepository journalLineRepository;
    private final AccountRepository accountRepository;

    /**
     * Calculates the current balance for a single account.
     *
     * DEBIT-normal: openingBalance + totalDebits - totalCredits
     * CREDIT-normal: openingBalance + totalCredits - totalDebits
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateCurrentBalance(String tenantId, UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        BigDecimal openingBalance = account.getOpeningBalance() != null ? account.getOpeningBalance() : BigDecimal.ZERO;

        List<Object[]> results = journalLineRepository.calculateAccountTotals(tenantId, accountId);
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        if (results != null && !results.isEmpty() && results.get(0) != null) {
            Object[] totals = results.get(0);
            if (totals.length >= 2) {
                totalDebits = totals[0] != null ? new BigDecimal(totals[0].toString()) : BigDecimal.ZERO;
                totalCredits = totals[1] != null ? new BigDecimal(totals[1].toString()) : BigDecimal.ZERO;
            }
        }

        if (account.getNormalBalance() == Account.NormalBalance.DEBIT) {
            return openingBalance.add(totalDebits).subtract(totalCredits);
        } else {
            return openingBalance.add(totalCredits).subtract(totalDebits);
        }
    }

    /**
     * Calculates current balances for ALL active accounts of a tenant.
     * Returns a map of accountId -> currentBalance.
     */
    @Transactional(readOnly = true)
    public Map<UUID, BigDecimal> calculateAllBalances(String tenantId) {
        List<Account> accounts = accountRepository.findByTenantIdAndIsActiveTrue(tenantId);
        Map<UUID, BigDecimal> balances = new HashMap<>();

        for (Account account : accounts) {
            balances.put(account.getId(), calculateCurrentBalance(tenantId, account.getId()));
        }
        return balances;
    }

    /**
     * Calculates the current balance for a single account within a specific fiscal year.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateBalanceForFiscalYear(String tenantId, UUID accountId, UUID fiscalYearId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        BigDecimal openingBalance = account.getOpeningBalance() != null ? account.getOpeningBalance() : BigDecimal.ZERO;

        List<Object[]> results = journalLineRepository.calculateAccountTotalsForFiscalYear(tenantId, accountId, fiscalYearId);
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        if (results != null && !results.isEmpty() && results.get(0) != null) {
            Object[] totals = results.get(0);
            if (totals.length >= 2) {
                totalDebits = totals[0] != null ? new BigDecimal(totals[0].toString()) : BigDecimal.ZERO;
                totalCredits = totals[1] != null ? new BigDecimal(totals[1].toString()) : BigDecimal.ZERO;
            }
        }

        if (account.getNormalBalance() == Account.NormalBalance.DEBIT) {
            return openingBalance.add(totalDebits).subtract(totalCredits);
        } else {
            return openingBalance.add(totalCredits).subtract(totalDebits);
        }
    }
}
