package com.financial.corefinance.service;

import com.financial.corefinance.domain.entity.FiscalYear;
import com.financial.corefinance.domain.entity.AccountingPeriod;
import com.financial.corefinance.exception.AccountValidationException;
import com.financial.corefinance.repository.FiscalYearRepository;
import com.financial.corefinance.repository.AccountingPeriodRepository;
import com.financial.corefinance.event.FinanceEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FiscalYearService {

    private final FiscalYearRepository fiscalYearRepository;
    private final AccountingPeriodRepository accountingPeriodRepository;
    private final com.financial.corefinance.repository.CalendarDefinitionRepository calendarDefinitionRepository;
    private final FinanceEventService financeEventService;

    @Transactional
    public FiscalYear createFiscalYear(@Valid FiscalYear fiscalYear) {
        log.info("Creating fiscal year: {}", fiscalYear.getYearNumber());
        
        // Validate year uniqueness
        String tenantId = fiscalYear.getTenantId();
        if (fiscalYearRepository.existsByTenantIdAndYearNumber(tenantId, fiscalYear.getYearNumber())) {
            throw new AccountValidationException("Fiscal year " + fiscalYear.getYearNumber() + " already exists");
        }
        
        // Validate date range
        if (fiscalYear.getStartDate() == null || fiscalYear.getEndDate() == null) {
            throw new AccountValidationException("Start date and end date are required");
        }
        
        if (fiscalYear.getStartDate().isAfter(fiscalYear.getEndDate())) {
            throw new AccountValidationException("Start date cannot be after end date");
        }

        // Validate calendar definition exists
        if (fiscalYear.getCalendarDefinitionId() != null) {
            boolean calendarExists = calendarDefinitionRepository.existsById(fiscalYear.getCalendarDefinitionId());
            if (!calendarExists) {
                throw new AccountValidationException("Calendar definition not found: " + fiscalYear.getCalendarDefinitionId());
            }
        } else {
            throw new AccountValidationException("Calendar definition is required");
        }
        
        // Validate no overlap with existing fiscal years
        validateNoDateOverlap(tenantId, fiscalYear);
        
        // Set default values
        if (fiscalYear.getIsCurrent() == null) {
            fiscalYear.setIsCurrent(false);
        }
        if (fiscalYear.getIsClosed() == null) {
            fiscalYear.setIsClosed(false);
        }
        if (fiscalYear.getTotalPeriods() == null) {
            fiscalYear.setTotalPeriods(12);
        }
        FiscalYear savedFiscalYear = fiscalYearRepository.save(fiscalYear);
        financeEventService.publishFiscalYearCreatedEvent(savedFiscalYear.getId(), savedFiscalYear.getTenantId(), "system");
        log.info("Fiscal year created successfully: {}", savedFiscalYear.getYearNumber());
        return savedFiscalYear;
    }

    @Transactional
    public AccountingPeriod createAccountingPeriod(@Valid AccountingPeriod period) {
        log.info("Creating accounting period: {} for fiscal year: {}", 
                period.getPeriodNumber(), period.getFiscalYearId());
        
        // Validate fiscal year exists
        Optional<FiscalYear> fiscalYearOpt = fiscalYearRepository.findById(period.getFiscalYearId());
        if (fiscalYearOpt.isEmpty()) {
            throw new AccountValidationException("Fiscal year not found: " + period.getFiscalYearId());
        }
        
        FiscalYear fiscalYear = fiscalYearOpt.get();
        
        // Validate period number uniqueness within fiscal year
        String tenantId = period.getTenantId();
        if (accountingPeriodRepository.existsByTenantIdAndFiscalYearIdAndPeriodNumber(
                tenantId, period.getFiscalYearId(), period.getPeriodNumber())) {
            throw new AccountValidationException("Period " + period.getPeriodNumber() + 
                " already exists for this fiscal year");
        }
        
        // Validate period dates are within fiscal year
        validatePeriodDatesWithinFiscalYear(period, fiscalYear);
        
        // Set default values
        if (period.getIsOpen() == null) {
            period.setIsOpen(true);
        }
        if (period.getIsClosed() == null) {
            period.setIsClosed(false);
        }
        if (period.getIsAdjustmentPeriod() == null) {
            period.setIsAdjustmentPeriod(false);
        }
        
        AccountingPeriod savedPeriod = accountingPeriodRepository.save(period);
        log.info("Accounting period created successfully: {}", savedPeriod.getPeriodName());
        return savedPeriod;
    }

    @Transactional
    public FiscalYear setCurrentFiscalYear(UUID fiscalYearId) {
        log.info("Setting current fiscal year: {}", fiscalYearId);
        
        Optional<FiscalYear> fiscalYearOpt = fiscalYearRepository.findById(fiscalYearId);
        if (fiscalYearOpt.isEmpty()) {
            throw new AccountValidationException("Fiscal year not found: " + fiscalYearId);
        }
        
        FiscalYear newCurrentFiscalYear = fiscalYearOpt.get();
        String tenantId = newCurrentFiscalYear.getTenantId();
        
        // Clear current flag from all fiscal years
        List<FiscalYear> allFiscalYears = fiscalYearRepository.findByTenantIdOrderByYearNumberDesc(tenantId);
        allFiscalYears.forEach(fy -> fy.setIsCurrent(false));
        fiscalYearRepository.saveAll(allFiscalYears);
        
        // Set new current fiscal year
        newCurrentFiscalYear.setIsCurrent(true);
        FiscalYear updatedFiscalYear = fiscalYearRepository.save(newCurrentFiscalYear);
        
        log.info("Current fiscal year set successfully: {}", updatedFiscalYear.getYearNumber());
        return updatedFiscalYear;
    }

    @Transactional
    public FiscalYear closeFiscalYear(UUID fiscalYearId, String closedBy) {
        log.info("Closing fiscal year: {}", fiscalYearId);
        
        Optional<FiscalYear> fiscalYearOpt = fiscalYearRepository.findById(fiscalYearId);
        if (fiscalYearOpt.isEmpty()) {
            throw new AccountValidationException("Fiscal year not found: " + fiscalYearId);
        }
        
        FiscalYear fiscalYear = fiscalYearOpt.get();
        
        if (fiscalYear.getIsClosed()) {
            throw new AccountValidationException("Fiscal year is already closed");
        }
        
        // Validate all periods are closed
        List<AccountingPeriod> openPeriods = accountingPeriodRepository.findOpenPeriodsByFiscalYear(
            fiscalYear.getTenantId(), fiscalYearId);
        if (!openPeriods.isEmpty()) {
            throw new AccountValidationException("Cannot close fiscal year with open periods. " +
                "Open periods: " + openPeriods.size());
        }
        
        fiscalYear.setIsClosed(true);
        fiscalYear.setClosedAt(LocalDate.now());
        fiscalYear.setClosedBy(closedBy);
        
        FiscalYear updatedFiscalYear = fiscalYearRepository.save(fiscalYear);
        financeEventService.publishFiscalYearClosedEvent(updatedFiscalYear.getId(), updatedFiscalYear.getTenantId(), closedBy);
        log.info("Fiscal year closed successfully: {}", updatedFiscalYear.getYearNumber());
        return updatedFiscalYear;
    }

    @Transactional
    public AccountingPeriod closeAccountingPeriod(UUID periodId, String closedBy) {
        log.info("Closing accounting period: {}", periodId);
        
        Optional<AccountingPeriod> periodOpt = accountingPeriodRepository.findById(periodId);
        if (periodOpt.isEmpty()) {
            throw new AccountValidationException("Accounting period not found: " + periodId);
        }
        
        AccountingPeriod period = periodOpt.get();
        
        if (period.getIsClosed()) {
            throw new AccountValidationException("Accounting period is already closed");
        }
        
        period.setIsClosed(true);
        period.setIsOpen(false);
        period.setClosedAt(LocalDate.now());
        period.setClosedBy(closedBy);
        
        AccountingPeriod updatedPeriod = accountingPeriodRepository.save(period);
        log.info("Accounting period closed successfully: {}", updatedPeriod.getPeriodName());
        return updatedPeriod;
    }

    @Transactional
    public AccountingPeriod openAccountingPeriod(UUID periodId) {
        log.info("Opening accounting period: {}", periodId);
        
        Optional<AccountingPeriod> periodOpt = accountingPeriodRepository.findById(periodId);
        if (periodOpt.isEmpty()) {
            throw new AccountValidationException("Accounting period not found: " + periodId);
        }
        
        AccountingPeriod period = periodOpt.get();
        
        // Check if fiscal year is closed
        Optional<FiscalYear> fiscalYearOpt = fiscalYearRepository.findById(period.getFiscalYearId());
        if (fiscalYearOpt.isPresent() && fiscalYearOpt.get().getIsClosed()) {
            throw new AccountValidationException("Cannot open period in closed fiscal year");
        }
        
        period.setIsClosed(false);
        period.setIsOpen(true);
        
        AccountingPeriod updatedPeriod = accountingPeriodRepository.save(period);
        log.info("Accounting period opened successfully: {}", updatedPeriod.getPeriodName());
        return updatedPeriod;
    }

    @Transactional(readOnly = true)
    public Optional<FiscalYear> getCurrentFiscalYear(String tenantId) {
        return fiscalYearRepository.findByTenantIdAndIsCurrentTrue(tenantId);
    }

    @Transactional(readOnly = true)
    public Optional<FiscalYear> getFiscalYearForDate(String tenantId, LocalDate date) {
        return fiscalYearRepository.findFiscalYearForDate(tenantId, date);
    }

    @Transactional(readOnly = true)
    public Optional<AccountingPeriod> getAccountingPeriodForDate(String tenantId, LocalDate date) {
        return accountingPeriodRepository.findPeriodForDate(tenantId, date);
    }

    @Transactional(readOnly = true)
    public List<FiscalYear> getOpenFiscalYears(String tenantId) {
        return fiscalYearRepository.findOpenFiscalYears(tenantId);
    }

    @Transactional(readOnly = true)
    public List<FiscalYear> getAllFiscalYears(String tenantId) {
        return fiscalYearRepository.findByTenantIdOrderByYearNumberDesc(tenantId);
    }

    @Transactional(readOnly = true)
    public Optional<FiscalYear> getFiscalYearById(UUID fiscalYearId) {
        return fiscalYearRepository.findById(fiscalYearId);
    }

    @Transactional(readOnly = true)
    public List<AccountingPeriod> getOpenAccountingPeriods(String tenantId) {
        return accountingPeriodRepository.findByTenantIdAndIsOpenTrue(tenantId);
    }

    @Transactional(readOnly = true)
    public List<AccountingPeriod> getAccountingPeriodsByFiscalYear(String tenantId, UUID fiscalYearId) {
        return accountingPeriodRepository.findByTenantIdAndFiscalYearIdOrderByPeriodNumber(tenantId, fiscalYearId);
    }

    @Transactional(readOnly = true)
    public Optional<AccountingPeriod> getAccountingPeriodById(UUID periodId) {
        return accountingPeriodRepository.findById(periodId);
    }

    @Transactional
    public List<AccountingPeriod> generateStandardPeriods(UUID fiscalYearId) {
        log.info("Generating standard accounting periods for fiscal year: {}", fiscalYearId);
        
        Optional<FiscalYear> fiscalYearOpt = fiscalYearRepository.findById(fiscalYearId);
        if (fiscalYearOpt.isEmpty()) {
            throw new AccountValidationException("Fiscal year not found: " + fiscalYearId);
        }
        
        FiscalYear fiscalYear = fiscalYearOpt.get();
        String tenantId = fiscalYear.getTenantId();
        
        // Clear existing periods
        List<AccountingPeriod> existingPeriods = accountingPeriodRepository.findByTenantIdAndFiscalYearIdOrderByPeriodNumber(tenantId, fiscalYearId);
        if (!existingPeriods.isEmpty()) {
            accountingPeriodRepository.deleteAll(existingPeriods);
        }
        
        // Generate 12 monthly periods
        LocalDate startDate = fiscalYear.getStartDate();
        List<AccountingPeriod> periods = new java.util.ArrayList<>();
        
        for (int i = 0; i < 12; i++) {
            LocalDate periodStart = startDate.plusMonths(i);
            LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());
            
            // Adjust end date if it's beyond fiscal year end
            if (periodEnd.isAfter(fiscalYear.getEndDate())) {
                periodEnd = fiscalYear.getEndDate();
            }
            
            AccountingPeriod period = AccountingPeriod.builder()
                .tenantId(tenantId)
                .fiscalYearId(fiscalYearId)
                .periodNumber(i + 1)
                .periodName("Period " + (i + 1) + " - " + periodStart.getMonth().toString())
                .startDate(periodStart)
                .endDate(periodEnd)
                .isOpen(true)
                .isClosed(false)
                .isAdjustmentPeriod(false)
                .build();
            
            periods.add(period);
        }
        
        // Save all periods
        List<AccountingPeriod> savedPeriods = accountingPeriodRepository.saveAll(periods);
        log.info("Generated {} standard accounting periods for fiscal year {}", savedPeriods.size(), fiscalYear.getYearNumber());
        return savedPeriods;
    }

    private void validateNoDateOverlap(String tenantId, FiscalYear fiscalYear) {
        List<FiscalYear> existingFiscalYears = fiscalYearRepository.findByTenantIdOrderByYearNumberDesc(tenantId);
        
        for (FiscalYear existing : existingFiscalYears) {
            if (datesOverlap(fiscalYear.getStartDate(), fiscalYear.getEndDate(), 
                           existing.getStartDate(), existing.getEndDate())) {
                throw new AccountValidationException("Fiscal year dates overlap with existing fiscal year: " + existing.getYearNumber());
            }
        }
    }

    private void validatePeriodDatesWithinFiscalYear(AccountingPeriod period, FiscalYear fiscalYear) {
        if (period.getStartDate().isBefore(fiscalYear.getStartDate()) || 
            period.getEndDate().isAfter(fiscalYear.getEndDate())) {
            throw new AccountValidationException("Period dates must be within fiscal year dates");
        }
        
        if (period.getStartDate().isAfter(period.getEndDate())) {
            throw new AccountValidationException("Period start date cannot be after end date");
        }
    }

    private boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }

    @Transactional(readOnly = true)
    public boolean isFiscalYearOpen(UUID fiscalYearId) {
        Optional<FiscalYear> fiscalYearOpt = fiscalYearRepository.findById(fiscalYearId);
        return fiscalYearOpt.map(fy -> !fy.getIsClosed()).orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isAccountingPeriodOpen(UUID periodId) {
        Optional<AccountingPeriod> periodOpt = accountingPeriodRepository.findById(periodId);
        return periodOpt.map(p -> p.getIsOpen() && !p.getIsClosed()).orElse(false);
    }

    @Transactional
    public com.financial.corefinance.domain.entity.CalendarDefinition createCalendarDefinition(com.financial.corefinance.domain.entity.CalendarDefinition definition) {
        log.info("Creating calendar definition: {}", definition.getCalendarName());
        
        if (calendarDefinitionRepository.existsByTenantIdAndCalendarName(definition.getTenantId(), definition.getCalendarName())) {
            throw new AccountValidationException("Calendar definition with name " + definition.getCalendarName() + " already exists");
        }
        
        if (definition.getIsDefault() != null && definition.getIsDefault()) {
            // Unset other defaults for this tenant
            List<com.financial.corefinance.domain.entity.CalendarDefinition> others = calendarDefinitionRepository.findByTenantId(definition.getTenantId());
            others.forEach(c -> c.setIsDefault(false));
            calendarDefinitionRepository.saveAll(others);
        }
        
        return calendarDefinitionRepository.save(definition);
    }

    @Transactional(readOnly = true)
    public List<com.financial.corefinance.domain.entity.CalendarDefinition> getAllCalendarDefinitions(String tenantId) {
        return calendarDefinitionRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public Optional<com.financial.corefinance.domain.entity.CalendarDefinition> getCalendarDefinitionById(UUID id) {
        return calendarDefinitionRepository.findById(id);
    }
}
