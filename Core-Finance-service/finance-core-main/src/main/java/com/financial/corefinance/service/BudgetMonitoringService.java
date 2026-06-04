package com.financial.corefinance.service;

import com.financial.corefinance.config.BudgetProperties;
import com.financial.corefinance.domain.base.TenantContext;
import com.financial.corefinance.domain.entity.Account;
import com.financial.corefinance.domain.entity.AccountingPeriod;
import com.financial.corefinance.domain.entity.Budget;
import com.financial.corefinance.domain.entity.BudgetChange;
import com.financial.corefinance.domain.entity.BudgetLine;
import com.financial.corefinance.domain.entity.BudgetLine.LineCategory;
import com.financial.corefinance.domain.entity.BudgetVersion;
import com.financial.corefinance.domain.entity.FiscalYear;
import com.financial.corefinance.domain.entity.JournalHeader;
import com.financial.corefinance.domain.entity.JournalLine;
import com.financial.corefinance.dto.request.BudgetForecastRequest;
import com.financial.corefinance.dto.request.BudgetTransferRequest;
import com.financial.corefinance.dto.response.BudgetForecastResponse;
import com.financial.corefinance.dto.response.BudgetMultiYearColumnResponse;
import com.financial.corefinance.dto.response.BudgetMultiYearRowResponse;
import com.financial.corefinance.dto.response.BudgetReportResponse;
import com.financial.corefinance.dto.response.BudgetVsActualLineResponse;
import com.financial.corefinance.exception.BudgetValidationException;
import com.financial.corefinance.repository.AccountRepository;
import com.financial.corefinance.repository.AccountingPeriodRepository;
import com.financial.corefinance.repository.BudgetChangeRepository;
import com.financial.corefinance.repository.BudgetLineRepository;
import com.financial.corefinance.repository.BudgetRepository;
import com.financial.corefinance.repository.BudgetVersionRepository;
import com.financial.corefinance.repository.FiscalYearRepository;
import com.financial.corefinance.dto.eventDto.FinanceEventDto;
import com.financial.corefinance.event.FinanceEventProducer;
import com.financial.corefinance.repository.JournalHeaderRepository;
import com.financial.corefinance.repository.JournalLineRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetMonitoringService {

    public static final String GROUP_DEPARTMENT = "DEPARTMENT";
    public static final String GROUP_ACCOUNT = "ACCOUNT";

    private final BudgetRepository budgetRepository;
    private final BudgetVersionRepository budgetVersionRepository;
    private final BudgetLineRepository budgetLineRepository;
    private final BudgetChangeRepository budgetChangeRepository;
    private final BudgetService budgetService;
    private final AccountRepository accountRepository;
    private final AccountingPeriodRepository accountingPeriodRepository;
    private final FiscalYearRepository fiscalYearRepository;
    private final JournalLineRepository journalLineRepository;
    private final JournalHeaderRepository journalHeaderRepository;
    private final BudgetProperties budgetProperties;
    private final FinanceEventProducer financeEventProducer;

    @Transactional
    public BudgetReportResponse getBudgetVsActual(
            UUID budgetId,
            UUID budgetVersionId,
            UUID departmentId,
            UUID accountId,
            LocalDate asOfDate,
            String groupBy) {
        String tenantId = TenantContext.getCurrentTenant();
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BudgetValidationException("Budget not found: " + budgetId));
        if (!tenantId.equals(budget.getTenantId())) {
            throw new BudgetValidationException("Budget not found for tenant");
        }

        List<BudgetLine> lines = resolveBudgetLines(budgetId, budgetVersionId).stream()
                .filter(l -> l.getLineCategory() == null || l.getLineCategory() == LineCategory.BUDGET)
                .filter(l -> departmentId == null || departmentId.equals(l.getDepartmentId())
                        || departmentId.equals(budget.getDepartmentId()))
                .filter(l -> accountId == null || accountId.equals(l.getAccountId()))
                .toList();

        LocalDate effectiveAsOf = asOfDate != null ? asOfDate : LocalDate.now();
        syncLineActualsFromGl(tenantId, budget.getFiscalYearId(), lines, effectiveAsOf);
        // Keep budget header and current version totalActualAmount in sync so list/versions pages
        // always reflect the latest computed actuals, not the stale 0 written at creation time.
        updateBudgetAndVersionTotals(budgetId, lines);

        String effectiveGroupBy = groupBy != null ? groupBy.toUpperCase() : GROUP_ACCOUNT;
        Map<UUID, Account> accountById = loadAccountsForLines(tenantId, lines);
        List<BudgetVsActualLineResponse> reportLines = buildGroupedLines(
                tenantId, budget, lines, effectiveAsOf, effectiveGroupBy, accountById);

        BigDecimal totalBudget = reportLines.stream()
                .map(BudgetVsActualLineResponse::getBudgetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalActual = reportLines.stream()
                .map(BudgetVsActualLineResponse::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalVariance = totalBudget.subtract(totalActual);

        BudgetVersion version = budgetVersionId != null
                ? budgetVersionRepository.findById(budgetVersionId).orElse(null)
                : budgetVersionRepository.findByBudgetIdAndIsCurrentTrue(budgetId).orElse(null);

        String monitoringNote = buildMonitoringNote(budget, version, lines, totalActual, effectiveAsOf);

        return BudgetReportResponse.builder()
                .budgetId(budgetId)
                .budgetName(budget.getBudgetName())
                .budgetVersionId(version != null ? version.getId() : null)
                .versionName(version != null ? version.getVersionName() : null)
                .fiscalYearId(budget.getFiscalYearId())
                .departmentId(departmentId != null ? departmentId : budget.getDepartmentId())
                .asOfDate(effectiveAsOf)
                .groupBy(effectiveGroupBy)
                .totalBudget(totalBudget)
                .totalActual(totalActual)
                .totalVariance(totalVariance)
                .totalUtilizationPercent(percent(totalActual, totalBudget))
                .lines(reportLines)
                .monitoringNote(monitoringNote)
                .build();
    }

    @Transactional(readOnly = true)
    public BudgetReportResponse getMidYearInquiry(UUID budgetId, LocalDate asOfDate, UUID departmentId) {
        return getBudgetVsActual(budgetId, null, departmentId, null, asOfDate, GROUP_DEPARTMENT);
    }

    @Transactional(readOnly = true)
    public List<BudgetMultiYearRowResponse> getMultiYearReport(List<UUID> fiscalYearIds, UUID departmentId) {
        String tenantId = TenantContext.getCurrentTenant();
        if (fiscalYearIds == null || fiscalYearIds.isEmpty()) {
            throw new BudgetValidationException("At least one fiscal year is required");
        }

        Map<String, BudgetMultiYearRowResponse> rows = new LinkedHashMap<>();

        for (UUID fiscalYearId : fiscalYearIds) {
            FiscalYear fy = fiscalYearRepository.findById(fiscalYearId)
                    .orElseThrow(() -> new BudgetValidationException("Fiscal year not found: " + fiscalYearId));
            String yearLabel = fy.getYearName() != null ? fy.getYearName() : fiscalYearId.toString();

            List<Budget> budgets = budgetRepository.findByTenantIdAndFiscalYearId(tenantId, fiscalYearId).stream()
                    .filter(b -> departmentId == null || departmentId.equals(b.getDepartmentId()))
                    .filter(b -> b.getStatus() == Budget.BudgetStatus.APPROVED
                            || b.getStatus() == Budget.BudgetStatus.ACTIVE)
                    .toList();

            for (Budget budget : budgets) {
                BudgetReportResponse report = getBudgetVsActual(
                        budget.getId(), null, departmentId, null, fy.getEndDate(), GROUP_DEPARTMENT);
                for (BudgetVsActualLineResponse line : report.getLines()) {
                    String key = line.getGroupKey() != null ? line.getGroupKey() : "TOTAL";
                    BudgetMultiYearRowResponse row = rows.computeIfAbsent(key, k -> BudgetMultiYearRowResponse.builder()
                            .departmentId(line.getDepartmentId())
                            .rowLabel(line.getGroupLabel())
                            .years(new ArrayList<>())
                            .build());
                    row.getYears().add(BudgetMultiYearColumnResponse.builder()
                            .fiscalYearId(fiscalYearId)
                            .fiscalYearLabel(yearLabel)
                            .budgetAmount(line.getBudgetAmount())
                            .actualAmount(line.getActualAmount())
                            .varianceAmount(line.getVarianceAmount())
                            .build());
                }
            }
        }
        return new ArrayList<>(rows.values());
    }

    @Transactional
    public List<BudgetChange> createBudgetTransfer(BudgetTransferRequest request, String requestedBy) {
        BudgetLine fromLine = budgetLineRepository.findById(request.getFromBudgetLineId())
                .orElseThrow(() -> new BudgetValidationException("Source budget line not found"));
        BudgetLine toLine = budgetLineRepository.findById(request.getToBudgetLineId())
                .orElseThrow(() -> new BudgetValidationException("Target budget line not found"));

        if (!fromLine.getBudgetId().equals(toLine.getBudgetId())) {
            throw new BudgetValidationException("Transfer lines must belong to the same budget");
        }
        BigDecimal fromAmount = fromLine.getBudgetAmount() != null ? fromLine.getBudgetAmount() : BigDecimal.ZERO;
        if (request.getAmount().compareTo(fromAmount) > 0) {
            throw new BudgetValidationException("Transfer amount exceeds source line budget of " + fromAmount);
        }

        BigDecimal toAmount = toLine.getBudgetAmount() != null ? toLine.getBudgetAmount() : BigDecimal.ZERO;

        BudgetChange transferOut = new BudgetChange();
        transferOut.setTenantId(fromLine.getTenantId());
        transferOut.setBudgetVersionId(request.getBudgetVersionId());
        transferOut.setBudgetLineId(fromLine.getId());
        transferOut.setChangeType(BudgetChange.ChangeType.TRANSFER_OUT);
        transferOut.setOldAmount(fromAmount);
        transferOut.setNewAmount(fromAmount.subtract(request.getAmount()));
        transferOut.setReason(request.getReason());
        transferOut.setAuthorityLevel(request.getAuthorityLevel());
        transferOut.setApprovedBy(requestedBy);
        transferOut.setEffectiveDate(LocalDate.now());

        BudgetChange transferIn = new BudgetChange();
        transferIn.setTenantId(toLine.getTenantId());
        transferIn.setBudgetVersionId(request.getBudgetVersionId());
        transferIn.setBudgetLineId(toLine.getId());
        transferIn.setChangeType(BudgetChange.ChangeType.TRANSFER_IN);
        transferIn.setOldAmount(toAmount);
        transferIn.setNewAmount(toAmount.add(request.getAmount()));
        transferIn.setReason(request.getReason());
        transferIn.setAuthorityLevel(request.getAuthorityLevel());
        transferIn.setApprovedBy(requestedBy);
        transferIn.setEffectiveDate(LocalDate.now());

        BudgetChange savedOut = budgetService.createBudgetChange(transferOut);
        BudgetChange savedIn = budgetService.createBudgetChange(transferIn);

        if (request.isAutoApprove()) {
            budgetService.approveBudgetChange(savedOut.getId(), requestedBy);
            budgetService.approveBudgetChange(savedIn.getId(), requestedBy);
            savedOut = budgetChangeRepository.findById(savedOut.getId()).orElse(savedOut);
            savedIn = budgetChangeRepository.findById(savedIn.getId()).orElse(savedIn);
        }

        financeEventPublishTransfer(savedOut, savedIn);
        return List.of(savedOut, savedIn);
    }

    @Transactional
    public BudgetLine saveForecast(BudgetForecastRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        if (request.getLineCategory() != LineCategory.REVENUE_FORECAST
                && request.getLineCategory() != LineCategory.EXPENDITURE_FORECAST) {
            throw new BudgetValidationException("Forecast category must be REVENUE_FORECAST or EXPENDITURE_FORECAST");
        }

        List<Budget> budgets = budgetRepository.findByTenantIdAndFiscalYearId(tenantId, request.getFiscalYearId());
        Budget budget = budgets.stream()
                .filter(b -> request.getDepartmentId() == null
                        || request.getDepartmentId().equals(b.getDepartmentId()))
                .findFirst()
                .orElseGet(() -> {
                    Budget b = new Budget();
                    b.setTenantId(tenantId);
                    b.setFiscalYearId(request.getFiscalYearId());
                    b.setBudgetName("Forecast " + request.getFiscalYearId());
                    b.setDepartmentId(request.getDepartmentId());
                    b.setBudgetType(Budget.BudgetType.OPERATING);
                    b.setStatus(Budget.BudgetStatus.DRAFT);
                    return budgetRepository.save(b);
                });

        if (request.getPriorYearActualAmount() == null && request.getAccountId() != null) {
            Optional<FiscalYear> priorYear = findPriorFiscalYear(tenantId, request.getFiscalYearId());
            if (priorYear.isPresent()) {
                BigDecimal prior = resolveGlActual(
                        tenantId,
                        request.getAccountId(),
                        priorYear.get().getId(),
                        priorYear.get().getEndDate(),
                        request.getDepartmentId(),
                        request.getLineCategory());
                request.setPriorYearActualAmount(prior);
            }
        }

        BudgetLine line = new BudgetLine();
        line.setTenantId(tenantId);
        line.setBudgetId(budget.getId());
        line.setAccountId(request.getAccountId());
        line.setDepartmentId(request.getDepartmentId());
        line.setPeriodNumber(request.getPeriodNumber());
        line.setLineCategory(request.getLineCategory());
        line.setBudgetAmount(request.getForecastAmount());
        line.setPriorYearActualAmount(request.getPriorYearActualAmount());
        line.setNotes(request.getNotes());
        line.calculateAmounts();
        return budgetLineRepository.save(line);
    }

    @Transactional(readOnly = true)
    public List<BudgetForecastResponse> listForecasts(UUID fiscalYearId, LineCategory category) {
        String tenantId = TenantContext.getCurrentTenant();
        List<BudgetLine> lines;
        if (category == null) {
            lines = new ArrayList<>();
            lines.addAll(budgetLineRepository.findForecastsByFiscalYear(
                    tenantId, fiscalYearId, LineCategory.REVENUE_FORECAST));
            lines.addAll(budgetLineRepository.findForecastsByFiscalYear(
                    tenantId, fiscalYearId, LineCategory.EXPENDITURE_FORECAST));
        } else {
            lines = budgetLineRepository.findForecastsByFiscalYear(tenantId, fiscalYearId, category);
        }
        return lines.stream().map(this::toForecastResponse).sorted(Comparator
                .comparing(BudgetForecastResponse::getLineCategory)
                .thenComparing(BudgetForecastResponse::getPeriodNumber))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getPriorYearActual(UUID fiscalYearId, UUID accountId, UUID departmentId) {
        String tenantId = TenantContext.getCurrentTenant();
        Optional<FiscalYear> priorYear = findPriorFiscalYear(tenantId, fiscalYearId);
        if (priorYear.isEmpty() || accountId == null) {
            return BigDecimal.ZERO;
        }
        return resolveGlActual(
                tenantId, accountId, priorYear.get().getId(), priorYear.get().getEndDate(), departmentId, LineCategory.EXPENDITURE_FORECAST);
    }

    public void validateJournalBudget(JournalHeader journalHeader) {
        if (journalHeader.getJournalLines() == null || journalHeader.getAccountingPeriodId() == null) {
            return;
        }
        AccountingPeriod period = accountingPeriodRepository.findById(journalHeader.getAccountingPeriodId())
                .orElse(null);
        if (period == null) {
            return;
        }

        String tenantId = journalHeader.getTenantId();
        List<Budget> budgets = budgetRepository.findByTenantIdAndFiscalYearId(tenantId, period.getFiscalYearId());
        Optional<Budget> monitoredBudget = budgets.stream().filter(this::isBudgetMonitored).findFirst();
        if (monitoredBudget.isEmpty()) {
            return;
        }

        List<BudgetLine> budgetLines = resolveBudgetLines(monitoredBudget.get().getId(), null);
        LocalDate asOf = journalHeader.getJournalDate() != null ? journalHeader.getJournalDate() : LocalDate.now();
        syncLineActualsFromGl(tenantId, period.getFiscalYearId(), budgetLines, asOf);

        int threshold = budgetProperties.control().warningThresholdPercent();

        for (JournalLine jl : journalHeader.getJournalLines()) {
            if (jl.getDebitAmount() == null || jl.getDebitAmount().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            Account account = accountRepository.findById(jl.getAccountId()).orElse(null);
            if (account == null || account.getAccountType() != Account.AccountType.EXPENSE) {
                continue;
            }

            Optional<BudgetLine> match = budgetLines.stream()
                    .filter(bl -> bl.getAccountId().equals(jl.getAccountId()))
                    .filter(bl -> jl.getDepartmentId() == null
                            || bl.getDepartmentId() == null
                            || jl.getDepartmentId().equals(bl.getDepartmentId()))
                    .findFirst();
            if (match.isEmpty()) {
                continue;
            }

            BudgetLine bl = match.get();
            BigDecimal budgetAmt = bl.getBudgetAmount() != null ? bl.getBudgetAmount() : BigDecimal.ZERO;
            BigDecimal projectedActual = (bl.getActualAmount() != null ? bl.getActualAmount() : BigDecimal.ZERO)
                    .add(jl.getDebitAmount());
            BigDecimal available = budgetAmt.subtract(projectedActual);

            if (available.compareTo(BigDecimal.ZERO) < 0) {
                String msg = String.format(
                        "Budget overrun on account %s: available %s, posting %s",
                        account.getAccountCode(), available, jl.getDebitAmount());
                if (budgetProperties.control().blockOnOverspend()) {
                    throw new BudgetValidationException(msg);
                }
                log.warn("Budget warning (BLOCK disabled): {}", msg);
            } else if (budgetAmt.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal utilization = projectedActual
                        .multiply(BigDecimal.valueOf(100))
                        .divide(budgetAmt, 2, RoundingMode.HALF_UP);
                if (utilization.intValue() >= threshold) {
                    log.warn(
                            "Budget utilization {}% on account {} (threshold {}%)",
                            utilization,
                            account.getAccountCode(),
                            threshold);
                }
            }
        }
    }

    @Transactional
    public void refreshBudgetActuals(UUID budgetId, LocalDate asOfDate) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BudgetValidationException("Budget not found: " + budgetId));
        String tenantId = budget.getTenantId();
        LocalDate asOf = asOfDate != null ? asOfDate : LocalDate.now();
        List<BudgetLine> lines = resolveBudgetLines(budgetId, null);
        syncLineActualsFromGl(tenantId, budget.getFiscalYearId(), lines, asOf);
        updateBudgetAndVersionTotals(budgetId, lines);
    }

    @Transactional
    public void refreshFiscalYearActuals(UUID fiscalYearId, LocalDate asOfDate) {
        String tenantId = TenantContext.getCurrentTenant();
        LocalDate asOf = asOfDate != null ? asOfDate : LocalDate.now();
        for (Budget budget : budgetRepository.findByTenantIdAndFiscalYearId(tenantId, fiscalYearId)) {
            if (budget.getStatus() == Budget.BudgetStatus.REJECTED
                    || budget.getStatus() == Budget.BudgetStatus.CLOSED
                    || budget.getStatus() == Budget.BudgetStatus.ARCHIVED) {
                continue;
            }
            List<BudgetLine> lines = resolveBudgetLines(budget.getId(), null);
            syncLineActualsFromGl(tenantId, fiscalYearId, lines, asOf);
            updateBudgetAndVersionTotals(budget.getId(), lines);
        }
    }

    @Transactional
    public void applyJournalToBudget(JournalHeader postedJournal) {
        if (postedJournal.getId() != null) {
            postedJournal = journalHeaderRepository.findByIdWithLines(postedJournal.getId()).orElse(postedJournal);
        }
        if (postedJournal.getAccountingPeriodId() == null) {
            return;
        }
        AccountingPeriod period = accountingPeriodRepository.findById(postedJournal.getAccountingPeriodId())
                .orElse(null);
        if (period == null) {
            return;
        }

        String tenantId = postedJournal.getTenantId();
        LocalDate asOf = postedJournal.getJournalDate() != null ? postedJournal.getJournalDate() : LocalDate.now();
        UUID fiscalYearId = period.getFiscalYearId();

        List<Budget> budgets = budgetRepository.findByTenantIdAndFiscalYearId(tenantId, fiscalYearId);
        for (Budget budget : budgets) {
            if (budget.getStatus() == Budget.BudgetStatus.REJECTED
                    || budget.getStatus() == Budget.BudgetStatus.CLOSED
                    || budget.getStatus() == Budget.BudgetStatus.ARCHIVED) {
                continue;
            }
            List<BudgetLine> lines = resolveBudgetLines(budget.getId(), null);
            syncLineActualsFromGl(tenantId, fiscalYearId, lines, asOf);
            updateBudgetAndVersionTotals(budget.getId(), lines);
        }
        log.debug("Budget actuals refreshed after journal post for fiscal year {}", fiscalYearId);
    }

    private void updateBudgetAndVersionTotals(UUID budgetId, List<BudgetLine> lines) {
        budgetService.updateBudgetTotalsFromLines(budgetId, lines);
    }

    private List<BudgetLine> resolveBudgetLines(UUID budgetId, UUID budgetVersionId) {
        if (budgetVersionId != null) {
            return budgetLineRepository.findByBudgetVersionIdAndLineCategory(budgetVersionId, LineCategory.BUDGET);
        }
        Optional<BudgetVersion> current = budgetVersionRepository.findByBudgetIdAndIsCurrentTrue(budgetId);
        if (current.isPresent()) {
            List<BudgetLine> versionLines =
                    budgetLineRepository.findByBudgetVersionIdAndLineCategory(current.get().getId(), LineCategory.BUDGET);
            if (!versionLines.isEmpty()) {
                return versionLines;
            }
        }
        return budgetLineRepository.findByBudgetIdAndLineCategory(budgetId, LineCategory.BUDGET);
    }

    private void syncLineActualsFromGl(
            String tenantId, UUID fiscalYearId, List<BudgetLine> lines, LocalDate asOfDate) {
        for (BudgetLine line : lines) {
            if (line.getAccountId() == null) {
                continue;
            }
            LineCategory cat = line.getLineCategory() != null ? line.getLineCategory() : LineCategory.BUDGET;
            BigDecimal actual = resolveGlActual(
                    tenantId, line.getAccountId(), fiscalYearId, asOfDate, line.getDepartmentId(), cat);
            line.setActualAmount(actual);
            line.calculateAmounts();
            budgetLineRepository.save(line);
        }
    }

    private BigDecimal resolveGlActual(
            String tenantId,
            UUID accountId,
            UUID fiscalYearId,
            LocalDate asOfDate,
            UUID departmentId,
            LineCategory category) {
        Account account = resolveAccount(tenantId, accountId);
        LocalDate effectiveAsOf = asOfDate != null ? asOfDate : LocalDate.now();

        BigDecimal fromDateRange = sumPostedActivityForFiscalYearDates(
                tenantId, accountId, fiscalYearId, effectiveAsOf, account);
        if (fromDateRange.compareTo(BigDecimal.ZERO) > 0) {
            return fromDateRange;
        }

        List<Object[]> byPeriod = journalLineRepository.calculateAccountTotalsForFiscalYearAsOf(
                tenantId, accountId, fiscalYearId, effectiveAsOf);
        return totalsRowToActual(account, byPeriod);
    }

    private BigDecimal sumPostedActivityForFiscalYearDates(
            String tenantId, UUID accountId, UUID fiscalYearId, LocalDate asOfDate, Account account) {
        FiscalYear fy = fiscalYearRepository.findById(fiscalYearId).orElse(null);
        if (fy == null || fy.getStartDate() == null || fy.getEndDate() == null) {
            return BigDecimal.ZERO;
        }
        LocalDate periodEnd = asOfDate.isAfter(fy.getEndDate()) ? fy.getEndDate() : asOfDate;
        if (periodEnd.isBefore(fy.getStartDate())) {
            return BigDecimal.ZERO;
        }
        // Use the budget-specific query that excludes CLOSING and OPENING_BALANCE entries.
        // Year-end transfers to retained earnings are not operational spend and must not
        // appear as budget actuals, even when their reversal counterpart is also present.
        List<Object[]> totals = journalLineRepository.calculateBudgetActualsForDateRange(
                tenantId, accountId, fy.getStartDate(), periodEnd);
        return totalsRowToActual(account, totals);
    }

    private BigDecimal resolveGlActualAllTime(String tenantId, UUID accountId) {
        Account account = resolveAccount(tenantId, accountId);
        List<Object[]> totals = journalLineRepository.calculateAccountTotals(tenantId, accountId);
        return totalsRowToActual(account, totals);
    }

    private BigDecimal totalsRowToActual(Account account, List<Object[]> totals) {
        if (totals == null || totals.isEmpty() || totals.get(0) == null) {
            return BigDecimal.ZERO;
        }
        Object[] row = totals.get(0);
        BigDecimal debit = toBigDecimal(row[0]);
        BigDecimal credit = toBigDecimal(row[1]);
        if (account != null && account.getAccountType() == Account.AccountType.REVENUE) {
            return credit.subtract(debit).max(BigDecimal.ZERO);
        }
        return debit.subtract(credit).max(BigDecimal.ZERO);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        return new BigDecimal(value.toString());
    }

    private Account resolveAccount(String tenantId, UUID accountId) {
        if (accountId == null) {
            return null;
        }
        return accountRepository.findById(accountId)
                .filter(a -> tenantId == null || tenantId.equals(a.getTenantId()))
                .orElse(null);
    }

    private Map<UUID, Account> loadAccountsForLines(String tenantId, List<BudgetLine> lines) {
        List<UUID> ids = lines.stream()
                .map(BudgetLine::getAccountId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return accountRepository.findAllById(ids).stream()
                .filter(a -> tenantId == null || tenantId.equals(a.getTenantId()))
                .collect(Collectors.toMap(Account::getId, Function.identity(), (a, b) -> a));
    }

    private List<BudgetVsActualLineResponse> buildGroupedLines(
            String tenantId,
            Budget budget,
            List<BudgetLine> lines,
            LocalDate asOfDate,
            String groupBy,
            Map<UUID, Account> accountById) {
        Map<String, List<BudgetLine>> grouped = new LinkedHashMap<>();
        for (BudgetLine line : lines) {
            String key;
            if (GROUP_DEPARTMENT.equals(groupBy)) {
                UUID dept = line.getDepartmentId() != null ? line.getDepartmentId() : budget.getDepartmentId();
                key = dept != null ? dept.toString() : "UNASSIGNED";
            } else {
                key = line.getAccountId() != null ? line.getAccountId().toString() : line.getId().toString();
            }
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(line);
        }

        List<BudgetVsActualLineResponse> result = new ArrayList<>();
        int threshold = budgetProperties.control().warningThresholdPercent();

        for (Map.Entry<String, List<BudgetLine>> entry : grouped.entrySet()) {
            BigDecimal budgetTotal = entry.getValue().stream()
                    .map(l -> l.getBudgetAmount() != null ? l.getBudgetAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal actualTotal = entry.getValue().stream()
                    .map(l -> l.getActualAmount() != null ? l.getActualAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal variance = budgetTotal.subtract(actualTotal);
            BigDecimal utilization = percent(actualTotal, budgetTotal);

            BudgetLine sample = entry.getValue().get(0);
            Account account = sample.getAccountId() != null ? accountById.get(sample.getAccountId()) : null;
            String label = resolveGroupLabel(groupBy, sample, budget, account);
            UUID deptId = sample.getDepartmentId() != null ? sample.getDepartmentId() : budget.getDepartmentId();

            String warning = null;
            if (variance.compareTo(BigDecimal.ZERO) < 0) {
                warning = "OVER";
            } else if (utilization != null && utilization.intValue() >= threshold) {
                warning = "WARN";
            }

            result.add(BudgetVsActualLineResponse.builder()
                    .budgetLineId(sample.getId())
                    .accountId(sample.getAccountId())
                    .accountCode(account != null ? account.getAccountCode() : null)
                    .accountName(account != null ? account.getAccountName() : null)
                    .departmentId(deptId)
                    .costCenterId(sample.getCostCenterId())
                    .groupKey(entry.getKey())
                    .groupLabel(label)
                    .budgetAmount(budgetTotal)
                    .actualAmount(actualTotal)
                    .varianceAmount(variance)
                    .variancePercent(percent(variance, budgetTotal))
                    .utilizationPercent(utilization)
                    .overBudget(variance.compareTo(BigDecimal.ZERO) < 0)
                    .warningLevel(warning)
                    .build());
        }
        return result;
    }

    private String resolveGroupLabel(String groupBy, BudgetLine line, Budget budget, Account account) {
        if (GROUP_DEPARTMENT.equals(groupBy)) {
            UUID dept = line.getDepartmentId() != null ? line.getDepartmentId() : budget.getDepartmentId();
            return dept != null ? "Dept " + dept.toString().substring(0, 8) : "Unassigned";
        }
        if (account != null) {
            return account.getAccountCode()
                    + (account.getAccountName() != null ? " — " + account.getAccountName() : "");
        }
        // Account exists on the budget line but is not present in the Chart of Accounts.
        // Show a clear warning so the user knows to add it to the COA or correct the line.
        String idPrefix = line.getAccountId() != null
                ? line.getAccountId().toString().substring(0, 8)
                : "unknown";
        return "Unmapped Account [" + idPrefix + "]";
    }

    private String resolveAccountCode(String tenantId, UUID accountId) {
        Account account = resolveAccount(tenantId, accountId);
        return account != null ? account.getAccountCode() : null;
    }

    private String resolveAccountName(String tenantId, UUID accountId) {
        Account account = resolveAccount(tenantId, accountId);
        return account != null ? account.getAccountName() : null;
    }

    private String buildMonitoringNote(
            Budget budget,
            BudgetVersion version,
            List<BudgetLine> lines,
            BigDecimal totalActual,
            LocalDate asOfDate) {
        if (version == null) {
            return "No current budget version. Approve a version and use Set as current on Budget versions.";
        }
        if (version.getStatus() != BudgetVersion.BudgetVersionStatus.APPROVED) {
            return "Current version is not APPROVED. Approve it before monitoring actuals.";
        }
        if (lines.isEmpty()) {
            return "Current version has no budget lines. Import CSV with GL account codes from your chart of accounts.";
        }
        long withoutAccount = lines.stream().filter(l -> l.getAccountId() == null).count();
        if (withoutAccount > 0) {
            return withoutAccount + " line(s) missing GL account — actuals cannot be calculated for those rows.";
        }

        String tenantId = budget.getTenantId();
        FiscalYear fy = fiscalYearRepository.findById(budget.getFiscalYearId()).orElse(null);
        String fyLabel = fy != null ? fy.getYearName() : budget.getFiscalYearId().toString();
        String fyDates = fy != null && fy.getStartDate() != null && fy.getEndDate() != null
                ? fy.getStartDate() + " to " + fy.getEndDate()
                : "dates not set";

        if (totalActual != null && totalActual.compareTo(BigDecimal.ZERO) == 0) {
            StringBuilder mismatch = new StringBuilder();
            for (BudgetLine line : lines) {
                if (line.getAccountId() == null) {
                    continue;
                }
                BigDecimal inFy = resolveGlActual(
                        tenantId, line.getAccountId(), budget.getFiscalYearId(), asOfDate, null, LineCategory.BUDGET);
                BigDecimal allTime = resolveGlActualAllTime(tenantId, line.getAccountId());
                String code = resolveAccountCode(tenantId, line.getAccountId());
                String label = code != null ? code : line.getAccountId().toString().substring(0, 8);
                if (allTime.compareTo(BigDecimal.ZERO) > 0 && inFy.compareTo(BigDecimal.ZERO) == 0) {
                    mismatch.append(" ")
                            .append(label)
                            .append(" has GL activity (")
                            .append(allTime.stripTrailingZeros().toPlainString())
                            .append(" all-time) but none in fiscal year ")
                            .append(fyLabel)
                            .append(". ");
                } else if (allTime.compareTo(BigDecimal.ZERO) == 0) {
                    mismatch.append(" ").append(label).append(": no posted journals yet. ");
                }
            }
            if (!mismatch.isEmpty()) {
                return "Budget fiscal year: " + fyLabel + " (" + fyDates + ")."
                        + mismatch
                        + "COA 'Current Balance' is all-time; budget actuals use this fiscal year's journal dates."
                        + " Create the budget under the same FY as your AP/AR posting period, or import lines for the GL accounts you post to.";
            }
        }

        return "Actuals = posted debits/credits on each line's GL account between "
                + fyDates
                + " (fiscal year "
                + fyLabel
                + ").";
    }

    private BigDecimal percent(BigDecimal part, BigDecimal whole) {
        if (whole == null || whole.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return part.multiply(BigDecimal.valueOf(100)).divide(whole, 2, RoundingMode.HALF_UP);
    }

    private Optional<FiscalYear> findPriorFiscalYear(String tenantId, UUID fiscalYearId) {
        return fiscalYearRepository.findById(fiscalYearId)
                .flatMap(current -> fiscalYearRepository.findByTenantIdOrderByYearNumberDesc(tenantId).stream()
                        .filter(fy -> fy.getStartDate() != null && current.getStartDate() != null
                                && fy.getStartDate().isBefore(current.getStartDate()))
                        .max(Comparator.comparing(FiscalYear::getStartDate)));
    }

    private BudgetForecastResponse toForecastResponse(BudgetLine line) {
        String tenantId = line.getTenantId() != null ? line.getTenantId() : TenantContext.getCurrentTenant();
        return BudgetForecastResponse.builder()
                .id(line.getId())
                .fiscalYearId(budgetRepository.findById(line.getBudgetId())
                        .map(Budget::getFiscalYearId)
                        .orElse(null))
                .departmentId(line.getDepartmentId())
                .accountId(line.getAccountId())
                .accountCode(resolveAccountCode(tenantId, line.getAccountId()))
                .accountName(resolveAccountName(tenantId, line.getAccountId()))
                .lineCategory(line.getLineCategory())
                .periodNumber(line.getPeriodNumber())
                .forecastAmount(line.getBudgetAmount())
                .priorYearActualAmount(line.getPriorYearActualAmount())
                .notes(line.getNotes())
                .build();
    }

    private boolean isBudgetMonitored(Budget budget) {
        if (budget.getStatus() == Budget.BudgetStatus.REJECTED
                || budget.getStatus() == Budget.BudgetStatus.CLOSED
                || budget.getStatus() == Budget.BudgetStatus.ARCHIVED) {
            return false;
        }
        return budgetVersionRepository.findByBudgetIdAndIsCurrentTrue(budget.getId())
                .map(v -> v.getStatus() == BudgetVersion.BudgetVersionStatus.APPROVED)
                .orElse(budget.getStatus() == Budget.BudgetStatus.APPROVED
                        || budget.getStatus() == Budget.BudgetStatus.ACTIVE);
    }

    private void financeEventPublishTransfer(BudgetChange out, BudgetChange in) {
        log.info("Budget transfer recorded: OUT {} IN {}", out.getId(), in.getId());
        budgetLineRepository.findById(out.getBudgetLineId()).ifPresent(line -> {
            FinanceEventDto event = FinanceEventDto.budgetUpdated(line.getBudgetId(), out.getTenantId(), out.getApprovedBy());
            financeEventProducer.sendUpdateBudgetEvent(event);
        });
    }
}
