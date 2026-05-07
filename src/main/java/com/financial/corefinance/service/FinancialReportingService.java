package com.financial.corefinance.service;

import com.financial.corefinance.domain.entity.*;
import com.financial.corefinance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialReportingService {

    private final FinancialReportRepository financialReportRepository;
    private final ReportLineRepository reportLineRepository;
    private final AccountRepository accountRepository;
    private final JournalHeaderRepository journalHeaderRepository;
    private final JournalLineRepository journalLineRepository;
    private final FiscalYearRepository fiscalYearRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public FinancialReport generateStatementOfFinancialPosition(UUID fiscalYearId, LocalDate asOfDate, String generatedBy) {
        log.info("Generating Statement of Financial Position for fiscal year: {} as of: {}", fiscalYearId, asOfDate);
        
        // Validate fiscal year
        FiscalYear fiscalYear = fiscalYearRepository.findById(fiscalYearId)
            .orElseThrow(() -> new IllegalArgumentException("Fiscal year not found: " + fiscalYearId));
        
        String tenantId = fiscalYear.getTenantId();
        
        // Create financial report
        FinancialReport report = FinancialReport.builder()
            .tenantId(tenantId)
            .fiscalYearId(fiscalYearId)
            .reportName("Statement of Financial Position")
            .reportType(FinancialReport.ReportType.STATEMENT_OF_FINANCIAL_POSITION)
            .reportPeriod("ANNUAL")
            .asOfDate(asOfDate)
            .reportDate(LocalDate.now())
            .currencyCode("USD")
            .status(FinancialReport.ReportStatus.GENERATING)
            .generatedBy(generatedBy)
            .build();
        
        FinancialReport savedReport = financialReportRepository.save(report);
        
        // Generate report lines
        List<ReportLine> reportLines = generateBalanceSheetLines(tenantId, fiscalYearId, asOfDate, savedReport.getId());
        
        // Save report lines
        reportLineRepository.saveAll(reportLines);
        
        // Update report status
        savedReport.setStatus(FinancialReport.ReportStatus.GENERATED);
        savedReport.setGeneratedAt(LocalDate.now());
        financialReportRepository.save(savedReport);
        
        // Create audit log
        createAuditLog(savedReport, "Statement of Financial Position generated", generatedBy);
        
        log.info("Statement of Financial Position generated successfully");
        return savedReport;
    }

    @Transactional
    public FinancialReport generateProfitAndLossStatement(UUID fiscalYearId, Integer periodNumber, String generatedBy) {
        log.info("Generating Profit and Loss Statement for fiscal year: {} period: {}", fiscalYearId, periodNumber);
        
        FiscalYear fiscalYear = fiscalYearRepository.findById(fiscalYearId)
            .orElseThrow(() -> new IllegalArgumentException("Fiscal year not found: " + fiscalYearId));
        
        String tenantId = fiscalYear.getTenantId();
        LocalDate reportDate = periodNumber != null ? 
            fiscalYear.getStartDate().plusMonths(periodNumber - 1) : fiscalYear.getEndDate();
        
        // Create financial report
        FinancialReport report = FinancialReport.builder()
            .tenantId(tenantId)
            .fiscalYearId(fiscalYearId)
            .reportName("Profit and Loss and Other Comprehensive Income")
            .reportType(FinancialReport.ReportType.PROFIT_LOSS_AND_OCI)
            .reportPeriod(periodNumber != null ? "QUARTERLY" : "ANNUAL")
            .periodNumber(periodNumber)
            .asOfDate(reportDate)
            .reportDate(LocalDate.now())
            .currencyCode("USD")
            .status(FinancialReport.ReportStatus.GENERATING)
            .generatedBy(generatedBy)
            .build();
        
        FinancialReport savedReport = financialReportRepository.save(report);
        
        // Generate report lines
        List<ReportLine> reportLines = generateProfitAndLossLines(tenantId, fiscalYearId, periodNumber, savedReport.getId());
        
        // Save report lines
        reportLineRepository.saveAll(reportLines);
        
        // Update report status
        savedReport.setStatus(FinancialReport.ReportStatus.GENERATED);
        savedReport.setGeneratedAt(LocalDate.now());
        financialReportRepository.save(savedReport);
        
        // Create audit log
        createAuditLog(savedReport, "Profit and Loss Statement generated", generatedBy);
        
        log.info("Profit and Loss Statement generated successfully");
        return savedReport;
    }

    @Transactional
    public FinancialReport generateCashFlowStatement(UUID fiscalYearId, Integer periodNumber, String generatedBy) {
        log.info("Generating Cash Flow Statement for fiscal year: {} period: {}", fiscalYearId, periodNumber);
        
        FiscalYear fiscalYear = fiscalYearRepository.findById(fiscalYearId)
            .orElseThrow(() -> new IllegalArgumentException("Fiscal year not found: " + fiscalYearId));
        
        String tenantId = fiscalYear.getTenantId();
        
        // Create financial report
        FinancialReport report = FinancialReport.builder()
            .tenantId(tenantId)
            .fiscalYearId(fiscalYearId)
            .reportName("Cash Flow Statement")
            .reportType(FinancialReport.ReportType.CASH_FLOW_STATEMENT)
            .reportPeriod(periodNumber != null ? "QUARTERLY" : "ANNUAL")
            .periodNumber(periodNumber)
            .asOfDate(fiscalYear.getEndDate())
            .reportDate(LocalDate.now())
            .currencyCode("USD")
            .status(FinancialReport.ReportStatus.GENERATING)
            .generatedBy(generatedBy)
            .build();
        
        FinancialReport savedReport = financialReportRepository.save(report);
        
        // Generate report lines
        List<ReportLine> reportLines = generateCashFlowLines(tenantId, fiscalYearId, periodNumber, savedReport.getId());
        
        // Save report lines
        reportLineRepository.saveAll(reportLines);
        
        // Update report status
        savedReport.setStatus(FinancialReport.ReportStatus.GENERATED);
        savedReport.setGeneratedAt(LocalDate.now());
        financialReportRepository.save(savedReport);
        
        // Create audit log
        createAuditLog(savedReport, "Cash Flow Statement generated", generatedBy);
        
        log.info("Cash Flow Statement generated successfully");
        return savedReport;
    }

    private List<ReportLine> generateBalanceSheetLines(String tenantId, UUID fiscalYearId, LocalDate asOfDate, UUID reportId) {
        List<ReportLine> reportLines = new ArrayList<>();
        int lineNumber = 1;
        
        // ASSETS SECTION
        reportLines.add(createReportLine(reportId, lineNumber++, "ASSETS", null, null, null, "HEADING", 0, true));
        
        // Current Assets
        reportLines.add(createReportLine(reportId, lineNumber++, "Current Assets", null, null, null, "HEADING", 1, true));
        
        // Cash and Cash Equivalents
        BigDecimal cashBalance = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, asOfDate, Account.IFRSCategory.CURRENT_ASSETS, "CASH");
        reportLines.add(createReportLine(reportId, lineNumber++, "Cash and Cash Equivalents", null, cashBalance, cashBalance, "ACCOUNT", 2, false));
        
        // Accounts Receivable
        BigDecimal receivablesBalance = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, asOfDate, Account.IFRSCategory.CURRENT_ASSETS, "RECEIVABLES");
        reportLines.add(createReportLine(reportId, lineNumber++, "Accounts Receivable", null, receivablesBalance, receivablesBalance, "ACCOUNT", 2, false));
        
        // Inventory
        BigDecimal inventoryBalance = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, asOfDate, Account.IFRSCategory.CURRENT_ASSETS, "INVENTORY");
        reportLines.add(createReportLine(reportId, lineNumber++, "Inventory", null, inventoryBalance, inventoryBalance, "ACCOUNT", 2, false));
        
        // Other Current Assets
        BigDecimal otherCurrentAssets = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, asOfDate, Account.IFRSCategory.CURRENT_ASSETS, "OTHER");
        reportLines.add(createReportLine(reportId, lineNumber++, "Other Current Assets", null, otherCurrentAssets, otherCurrentAssets, "ACCOUNT", 2, false));
        
        // Total Current Assets
        BigDecimal totalCurrentAssets = cashBalance.add(receivablesBalance).add(inventoryBalance).add(otherCurrentAssets);
        reportLines.add(createReportLine(reportId, lineNumber++, "Total Current Assets", null, totalCurrentAssets, totalCurrentAssets, "SUBTOTAL", 1, true));
        
        // Non-Current Assets
        reportLines.add(createReportLine(reportId, lineNumber++, "Non-Current Assets", null, null, null, "HEADING", 1, true));
        
        // Property, Plant and Equipment
        BigDecimal ppeBalance = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, asOfDate, Account.IFRSCategory.NON_CURRENT_ASSETS, "PPE");
        reportLines.add(createReportLine(reportId, lineNumber++, "Property, Plant and Equipment", null, ppeBalance, ppeBalance, "ACCOUNT", 2, false));
        
        // Intangible Assets
        BigDecimal intangibleBalance = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, asOfDate, Account.IFRSCategory.NON_CURRENT_ASSETS, "INTANGIBLE");
        reportLines.add(createReportLine(reportId, lineNumber++, "Intangible Assets", null, intangibleBalance, intangibleBalance, "ACCOUNT", 2, false));
        
        // Total Non-Current Assets
        BigDecimal totalNonCurrentAssets = ppeBalance.add(intangibleBalance);
        reportLines.add(createReportLine(reportId, lineNumber++, "Total Non-Current Assets", null, totalNonCurrentAssets, totalNonCurrentAssets, "SUBTOTAL", 1, true));
        
        // Total Assets
        BigDecimal totalAssets = totalCurrentAssets.add(totalNonCurrentAssets);
        reportLines.add(createReportLine(reportId, lineNumber++, "TOTAL ASSETS", null, totalAssets, totalAssets, "TOTAL", 0, true));
        
        // LIABILITIES AND EQUITY SECTION
        reportLines.add(createReportLine(reportId, lineNumber++, "LIABILITIES AND EQUITY", null, null, null, "HEADING", 0, true));
        
        // Current Liabilities
        reportLines.add(createReportLine(reportId, lineNumber++, "Current Liabilities", null, null, null, "HEADING", 1, true));
        
        // Accounts Payable
        BigDecimal payablesBalance = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, asOfDate, Account.IFRSCategory.CURRENT_LIABILITIES, "PAYABLES");
        reportLines.add(createReportLine(reportId, lineNumber++, "Accounts Payable", null, payablesBalance, payablesBalance, "ACCOUNT", 2, false));
        
        // Short-term Loans
        BigDecimal shortTermLoans = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, asOfDate, Account.IFRSCategory.CURRENT_LIABILITIES, "LOANS");
        reportLines.add(createReportLine(reportId, lineNumber++, "Short-term Loans", null, shortTermLoans, shortTermLoans, "ACCOUNT", 2, false));
        
        // Total Current Liabilities
        BigDecimal totalCurrentLiabilities = payablesBalance.add(shortTermLoans);
        reportLines.add(createReportLine(reportId, lineNumber++, "Total Current Liabilities", null, totalCurrentLiabilities, totalCurrentLiabilities, "SUBTOTAL", 1, true));
        
        // Non-Current Liabilities
        reportLines.add(createReportLine(reportId, lineNumber++, "Non-Current Liabilities", null, null, null, "HEADING", 1, true));
        
        // Long-term Loans
        BigDecimal longTermLoans = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, asOfDate, Account.IFRSCategory.NON_CURRENT_LIABILITIES, "LOANS");
        reportLines.add(createReportLine(reportId, lineNumber++, "Long-term Loans", null, longTermLoans, longTermLoans, "ACCOUNT", 2, false));
        
        // Total Non-Current Liabilities
        BigDecimal totalNonCurrentLiabilities = longTermLoans;
        reportLines.add(createReportLine(reportId, lineNumber++, "Total Non-Current Liabilities", null, totalNonCurrentLiabilities, totalNonCurrentLiabilities, "SUBTOTAL", 1, true));
        
        // Total Liabilities
        BigDecimal totalLiabilities = totalCurrentLiabilities.add(totalNonCurrentLiabilities);
        reportLines.add(createReportLine(reportId, lineNumber++, "Total Liabilities", null, totalLiabilities, totalLiabilities, "SUBTOTAL", 1, true));
        
        // Equity
        reportLines.add(createReportLine(reportId, lineNumber++, "Equity", null, null, null, "HEADING", 1, true));
        
        // Share Capital
        BigDecimal shareCapital = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, asOfDate, Account.IFRSCategory.EQUITY, "CAPITAL");
        reportLines.add(createReportLine(reportId, lineNumber++, "Share Capital", null, shareCapital, shareCapital, "ACCOUNT", 2, false));
        
        // Retained Earnings
        BigDecimal retainedEarnings = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, asOfDate, Account.IFRSCategory.EQUITY, "RETAINED");
        reportLines.add(createReportLine(reportId, lineNumber++, "Retained Earnings", null, retainedEarnings, retainedEarnings, "ACCOUNT", 2, false));
        
        // Total Equity
        BigDecimal totalEquity = shareCapital.add(retainedEarnings);
        reportLines.add(createReportLine(reportId, lineNumber++, "Total Equity", null, totalEquity, totalEquity, "SUBTOTAL", 1, true));
        
        // Total Liabilities and Equity
        BigDecimal totalLiabilitiesAndEquity = totalLiabilities.add(totalEquity);
        reportLines.add(createReportLine(reportId, lineNumber++, "TOTAL LIABILITIES AND EQUITY", null, totalLiabilitiesAndEquity, totalLiabilitiesAndEquity, "TOTAL", 0, true));
        
        return reportLines;
    }

    private List<ReportLine> generateProfitAndLossLines(String tenantId, UUID fiscalYearId, Integer periodNumber, UUID reportId) {
        List<ReportLine> reportLines = new ArrayList<>();
        int lineNumber = 1;
        
        // REVENUE SECTION
        reportLines.add(createReportLine(reportId, lineNumber++, "REVENUE", null, null, null, "HEADING", 0, true));
        
        // Sales Revenue
        BigDecimal salesRevenue = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, null, Account.IFRSCategory.REVENUE, "SALES");
        reportLines.add(createReportLine(reportId, lineNumber++, "Sales Revenue", null, salesRevenue, salesRevenue, "ACCOUNT", 1, false));
        
        // Other Revenue
        BigDecimal otherRevenue = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, null, Account.IFRSCategory.OTHER_INCOME, "OTHER");
        reportLines.add(createReportLine(reportId, lineNumber++, "Other Revenue", null, otherRevenue, otherRevenue, "ACCOUNT", 1, false));
        
        // Total Revenue
        BigDecimal totalRevenue = salesRevenue.add(otherRevenue);
        reportLines.add(createReportLine(reportId, lineNumber++, "Total Revenue", null, totalRevenue, totalRevenue, "SUBTOTAL", 0, true));
        
        // EXPENSES SECTION
        reportLines.add(createReportLine(reportId, lineNumber++, "EXPENSES", null, null, null, "HEADING", 0, true));
        
        // Cost of Goods Sold
        BigDecimal cogs = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, null, Account.IFRSCategory.OPERATING_EXPENSES, "COGS");
        reportLines.add(createReportLine(reportId, lineNumber++, "Cost of Goods Sold", null, cogs, cogs, "ACCOUNT", 1, false));
        
        // Operating Expenses
        BigDecimal operatingExpenses = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, null, Account.IFRSCategory.OPERATING_EXPENSES, "OPERATING");
        reportLines.add(createReportLine(reportId, lineNumber++, "Operating Expenses", null, operatingExpenses, operatingExpenses, "ACCOUNT", 1, false));
        
        // Other Expenses
        BigDecimal otherExpenses = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, null, Account.IFRSCategory.OTHER_EXPENSES, "OTHER");
        reportLines.add(createReportLine(reportId, lineNumber++, "Other Expenses", null, otherExpenses, otherExpenses, "ACCOUNT", 1, false));
        
        // Total Expenses
        BigDecimal totalExpenses = cogs.add(operatingExpenses).add(otherExpenses);
        reportLines.add(createReportLine(reportId, lineNumber++, "Total Expenses", null, totalExpenses, totalExpenses, "SUBTOTAL", 0, true));
        
        // PROFIT/LOSS
        BigDecimal profitLoss = totalRevenue.subtract(totalExpenses);
        String profitLossLabel = profitLoss.compareTo(BigDecimal.ZERO) >= 0 ? "PROFIT BEFORE TAX" : "LOSS BEFORE TAX";
        reportLines.add(createReportLine(reportId, lineNumber++, profitLossLabel, null, profitLoss.abs(), profitLoss.abs(), "TOTAL", 0, true));
        
        return reportLines;
    }

    private List<ReportLine> generateCashFlowLines(String tenantId, UUID fiscalYearId, Integer periodNumber, UUID reportId) {
        List<ReportLine> reportLines = new ArrayList<>();
        int lineNumber = 1;
        
        // OPERATING ACTIVITIES
        reportLines.add(createReportLine(reportId, lineNumber++, "CASH FLOWS FROM OPERATING ACTIVITIES", null, null, null, "HEADING", 0, true));
        
        // Net Income (would come from P&L)
        BigDecimal netIncome = calculateNetIncome(tenantId, fiscalYearId, periodNumber);
        reportLines.add(createReportLine(reportId, lineNumber++, "Net Income", null, netIncome, netIncome, "ACCOUNT", 1, false));
        
        // Adjustments for non-cash items
        BigDecimal depreciation = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, null, Account.IFRSCategory.OPERATING_EXPENSES, "DEPRECIATION");
        reportLines.add(createReportLine(reportId, lineNumber++, "Depreciation", null, depreciation, depreciation, "ACCOUNT", 1, false));
        
        // Changes in working capital
        BigDecimal workingCapitalChange = calculateWorkingCapitalChange(tenantId, fiscalYearId, periodNumber);
        reportLines.add(createReportLine(reportId, lineNumber++, "Changes in Working Capital", null, workingCapitalChange, workingCapitalChange, "ACCOUNT", 1, false));
        
        // Net Cash from Operating Activities
        BigDecimal netCashFromOperations = netIncome.add(depreciation).add(workingCapitalChange);
        reportLines.add(createReportLine(reportId, lineNumber++, "Net Cash from Operating Activities", null, netCashFromOperations, netCashFromOperations, "SUBTOTAL", 0, true));
        
        // INVESTING ACTIVITIES
        reportLines.add(createReportLine(reportId, lineNumber++, "CASH FLOWS FROM INVESTING ACTIVITIES", null, null, null, "HEADING", 0, true));
        
        // Purchase of PPE
        BigDecimal ppePurchase = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, null, Account.IFRSCategory.NON_CURRENT_ASSETS, "PURCHASE");
        reportLines.add(createReportLine(reportId, lineNumber++, "Purchase of Property, Plant and Equipment", null, ppePurchase.negate(), ppePurchase.negate(), "ACCOUNT", 1, false));
        
        // Net Cash from Investing Activities
        reportLines.add(createReportLine(reportId, lineNumber++, "Net Cash from Investing Activities", null, ppePurchase.negate(), ppePurchase.negate(), "SUBTOTAL", 0, true));
        
        // FINANCING ACTIVITIES
        reportLines.add(createReportLine(reportId, lineNumber++, "CASH FLOWS FROM FINANCING ACTIVITIES", null, null, null, "HEADING", 0, true));
        
        // Proceeds from Loans
        BigDecimal loanProceeds = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, null, Account.IFRSCategory.NON_CURRENT_LIABILITIES, "PROCEEDS");
        reportLines.add(createReportLine(reportId, lineNumber++, "Proceeds from Long-term Loans", null, loanProceeds, loanProceeds, "ACCOUNT", 1, false));
        
        // Repayment of Loans
        BigDecimal loanRepayment = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, null, Account.IFRSCategory.NON_CURRENT_LIABILITIES, "REPAYMENT");
        reportLines.add(createReportLine(reportId, lineNumber++, "Repayment of Long-term Loans", null, loanRepayment.negate(), loanRepayment.negate(), "ACCOUNT", 1, false));
        
        // Net Cash from Financing Activities
        BigDecimal netCashFromFinancing = loanProceeds.subtract(loanRepayment);
        reportLines.add(createReportLine(reportId, lineNumber++, "Net Cash from Financing Activities", null, netCashFromFinancing, netCashFromFinancing, "SUBTOTAL", 0, true));
        
        // NET CHANGE IN CASH
        BigDecimal netChangeInCash = netCashFromOperations.add(ppePurchase.negate()).add(netCashFromFinancing);
        reportLines.add(createReportLine(reportId, lineNumber++, "NET CHANGE IN CASH", null, netChangeInCash, netChangeInCash, "TOTAL", 0, true));
        
        return reportLines;
    }

    private ReportLine createReportLine(UUID reportId, int lineNumber, String description, UUID accountId, 
                                      BigDecimal currentAmount, BigDecimal priorAmount, String lineType, 
                                      int indentationLevel, boolean isBold) {
        return ReportLine.builder()
            .financialReportId(reportId)
            .lineNumber(lineNumber)
            .accountId(accountId)
            .lineType(lineType)
            .lineDescription(description)
            .currentPeriodAmount(currentAmount)
            .priorPeriodAmount(priorAmount)
            .indentationLevel(indentationLevel)
            .isBold(isBold)
            .showZeroAmounts(false)
            .build();
    }

    private BigDecimal calculateAccountBalanceByIFRSCategory(String tenantId, UUID fiscalYearId, LocalDate asOfDate, 
                                                           Account.IFRSCategory ifrsCategory, String accountType) {
        List<Account> accounts = accountRepository.findByTenantIdAndIfrsCategory(tenantId, ifrsCategory);
        
        BigDecimal totalBalance = BigDecimal.ZERO;
        
        for (Account account : accounts) {
            if (account.getIsActive() && (accountType == null || 
                account.getAccountCode().toUpperCase().contains(accountType.toUpperCase()))) {
                BigDecimal accountBalance = calculateAccountBalanceToDate(account.getId(), fiscalYearId, asOfDate);
                
                // Adjust based on normal balance
                if (account.getNormalBalance() == Account.NormalBalance.CREDIT) {
                    accountBalance = accountBalance.negate();
                }
                
                totalBalance = totalBalance.add(accountBalance);
            }
        }
        
        return totalBalance.abs();
    }

    private BigDecimal calculateAccountBalanceToDate(UUID accountId, UUID fiscalYearId, LocalDate asOfDate) {
        // Calculate account balance from beginning of fiscal year to asOfDate
        List<JournalLine> journalLines = journalLineRepository.findPostedLinesByAccount(
            com.financial.corefinance.domain.base.TenantContext.getCurrentTenant(), accountId);
        
        BigDecimal balance = BigDecimal.ZERO;
        
        for (JournalLine line : journalLines) {
            // Check if the journal is within the date range
            Optional<JournalHeader> journalHeader = journalHeaderRepository.findById(line.getJournalHeaderId());
            if (journalHeader.isPresent()) {
                LocalDate journalDate = journalHeader.get().getJournalDate();
                if (!journalDate.isAfter(asOfDate)) {
                    if (line.getDebitAmount() != null) {
                        balance = balance.add(line.getDebitAmount());
                    }
                    if (line.getCreditAmount() != null) {
                        balance = balance.subtract(line.getCreditAmount());
                    }
                }
            }
        }
        
        return balance;
    }

    private BigDecimal calculateNetIncome(String tenantId, UUID fiscalYearId, Integer periodNumber) {
        // This would typically come from the P&L statement
        // For now, calculate revenue minus expenses
        BigDecimal revenue = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, null, Account.IFRSCategory.REVENUE, null);
        BigDecimal expenses = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, null, Account.IFRSCategory.OPERATING_EXPENSES, null);
        BigDecimal otherExpenses = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, null, Account.IFRSCategory.OTHER_EXPENSES, null);
        
        return revenue.subtract(expenses).subtract(otherExpenses);
    }

    private BigDecimal calculateWorkingCapitalChange(String tenantId, UUID fiscalYearId, Integer periodNumber) {
        // Calculate changes in current assets and current liabilities
        BigDecimal currentAssets = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, null, Account.IFRSCategory.CURRENT_ASSETS, null);
        BigDecimal currentLiabilities = calculateAccountBalanceByIFRSCategory(tenantId, fiscalYearId, null, Account.IFRSCategory.CURRENT_LIABILITIES, null);
        
        return currentAssets.subtract(currentLiabilities);
    }

    private void createAuditLog(FinancialReport report, String action, String performedBy) {
        AuditLog auditLog = AuditLog.builder()
            .tenantId(report.getTenantId())
            .entityType("FinancialReport")
            .entityId(report.getId())
            .action(AuditLog.AuditAction.CREATE)
            .newValue("Report: " + report.getReportName() + ", Action: " + action)
            .userId(performedBy)
            .additionalInfo("Financial report generation")
            .moduleName("FinancialReporting")
            .functionName("generateReport")
            .businessTransactionId(report.getReportName())
            .build();
        
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<FinancialReport> getReportsByFiscalYear(UUID fiscalYearId) {
        return financialReportRepository.findByTenantIdAndFiscalYearId(
            com.financial.corefinance.domain.base.TenantContext.getCurrentTenant(), fiscalYearId);
    }

    @Transactional(readOnly = true)
    public Optional<FinancialReport> getLatestReportByType(UUID fiscalYearId, FinancialReport.ReportType reportType) {
        List<FinancialReport> reports = financialReportRepository.findLatestReportsByType(
            com.financial.corefinance.domain.base.TenantContext.getCurrentTenant(), fiscalYearId, reportType);
        return reports.isEmpty() ? Optional.empty() : Optional.of(reports.get(0));
    }
}
