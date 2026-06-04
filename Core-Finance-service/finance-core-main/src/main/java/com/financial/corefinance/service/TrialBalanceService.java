package com.financial.corefinance.service;

import com.financial.corefinance.domain.entity.Account;
import com.financial.corefinance.dto.response.TrialBalanceLineResponse;
import com.financial.corefinance.dto.response.TrialBalanceReportResponse;
import com.financial.corefinance.repository.AccountRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrialBalanceService {

    private static final String AP_PAYABLE_CODE = "2100";
    private static final String AR_RECEIVABLE_CODE = "1200";

    private final AccountRepository accountRepository;
    private final GlReportingService glReportingService;
    private final IntegrationGlAccountService integrationGlAccountService;
    private final GlBalanceSignService glBalanceSignService;

    @Transactional(readOnly = true)
    public TrialBalanceReportResponse buildTrialBalance(String tenantId, LocalDate asOfDate) {
        integrationGlAccountService.seedIntegrationChart(tenantId);

        List<Account> accounts = accountRepository.findByTenantIdAndIsActiveTrue(tenantId);
        accounts.sort(Comparator.comparing(Account::getAccountCode, Comparator.nullsLast(String::compareTo)));

        List<TrialBalanceLineResponse> lines = new ArrayList<>();
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        TrialBalanceLineResponse apPayableLine = null;
        TrialBalanceLineResponse arReceivableLine = null;

        for (Account account : accounts) {
            BigDecimal balance = glReportingService.calculateCurrentBalance(tenantId, account.getId());
            if (balance.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            BigDecimal debit = BigDecimal.ZERO;
            BigDecimal credit = BigDecimal.ZERO;

            if (account.getNormalBalance() == Account.NormalBalance.DEBIT) {
                if (balance.compareTo(BigDecimal.ZERO) >= 0) {
                    debit = balance;
                } else {
                    credit = balance.abs();
                }
            } else {
                if (balance.compareTo(BigDecimal.ZERO) >= 0) {
                    credit = balance;
                } else {
                    debit = balance.abs();
                }
            }

            totalDebits = totalDebits.add(debit);
            totalCredits = totalCredits.add(credit);

            boolean abnormal = glBalanceSignService.isAbnormalBalance(account, balance);
            TrialBalanceLineResponse line = TrialBalanceLineResponse.builder()
                    .accountId(account.getId())
                    .accountCode(account.getAccountCode())
                    .accountName(account.getAccountName())
                    .accountType(account.getAccountType() != null ? account.getAccountType().name() : null)
                    .normalBalance(account.getNormalBalance() != null ? account.getNormalBalance().name() : null)
                    .debit(debit)
                    .credit(credit)
                    .balance(balance)
                    .integrationAccount(isIntegrationCode(account.getAccountCode()))
                    .abnormalBalance(abnormal)
                    .abnormalBalanceReason(abnormal ? glBalanceSignService.abnormalBalanceReason(account, balance) : null)
                    .build();

            lines.add(line);
            if (AP_PAYABLE_CODE.equals(account.getAccountCode())) {
                apPayableLine = line;
            }
            if (AR_RECEIVABLE_CODE.equals(account.getAccountCode())) {
                arReceivableLine = line;
            }
        }

        boolean balanced = totalDebits.subtract(totalCredits).abs().compareTo(new BigDecimal("0.01")) <= 0;

        return TrialBalanceReportResponse.builder()
                .asOfDate(asOfDate != null ? asOfDate : LocalDate.now())
                .totalDebits(totalDebits)
                .totalCredits(totalCredits)
                .balanced(balanced)
                .apPayableLine(apPayableLine)
                .arReceivableLine(arReceivableLine)
                .lines(lines)
                .build();
    }

    private static boolean isIntegrationCode(String code) {
        if (code == null) {
            return false;
        }
        return code.equals("2100")
                || code.equals("5100")
                || code.equals("6100")
                || code.equals("1150")
                || code.equals("1100")
                || code.equals("1200")
                || code.equals("4100")
                || code.equals("2200");
    }
}
