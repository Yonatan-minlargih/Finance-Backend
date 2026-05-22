package com.financial.corefinance.service;

import com.financial.corefinance.domain.base.TenantContext;
import com.financial.corefinance.domain.entity.Account;
import com.financial.corefinance.dto.response.DashboardMetricsResponse;
import com.financial.corefinance.repository.AccountRepository;
import com.financial.corefinance.repository.FinancialReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AccountRepository accountRepository;
    private final FinancialReportRepository financialReportRepository;

    @Transactional(readOnly = true)
    public DashboardMetricsResponse getDashboardMetrics() {
        String tenantId = TenantContext.getCurrentTenant();

        // 1. Active Accounts
        List<Account> activeAccountsList = accountRepository.findByTenantIdAndIsActiveTrue(tenantId);
        long activeAccounts = activeAccountsList.size();

        // 2. Total Balance (Summing opening balance of all Asset accounts)
        List<Account> assetAccounts = accountRepository.findByTenantIdAndAccountType(tenantId, Account.AccountType.ASSET);
        BigDecimal totalBalance = assetAccounts.stream()
                .map(Account::getOpeningBalance)
                .filter(balance -> balance != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Monthly Growth
        // In a real scenario, this would compare current month vs last month.
        BigDecimal monthlyGrowth = BigDecimal.ZERO;

        // 4. Total Reports
        long totalReports = financialReportRepository.findByTenantId(tenantId, PageRequest.of(0, 1)).getTotalElements();

        return DashboardMetricsResponse.builder()
                .totalBalance(totalBalance)
                .activeAccounts(activeAccounts)
                .monthlyGrowth(monthlyGrowth)
                .totalReports(totalReports)
                .build();
    }
}
