package com.financial.corefinance.controller;

import com.financial.corefinance.domain.entity.FiscalYear;
import com.financial.corefinance.domain.entity.AccountingPeriod;
import com.financial.corefinance.repository.FiscalYearRepository;
import com.financial.corefinance.repository.AccountingPeriodRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/periods")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fiscal Period Management", description = "APIs for managing fiscal years and accounting periods")
public class PeriodController {

    private final FiscalYearRepository fiscalYearRepository;
    private final AccountingPeriodRepository accountingPeriodRepository;

    // Fiscal Year Endpoints
    @PostMapping("/fiscal-years")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Create fiscal year", description = "Creates a new fiscal year")
    public ResponseEntity<FiscalYear> createFiscalYear(@Valid @RequestBody FiscalYear fiscalYear) {
        log.info("Creating fiscal year: {}", fiscalYear.getYearNumber());
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        if (fiscalYearRepository.existsByTenantIdAndYearNumber(tenantId, fiscalYear.getYearNumber())) {
            throw new IllegalArgumentException("Fiscal year " + fiscalYear.getYearNumber() + " already exists");
        }
        
        FiscalYear savedFiscalYear = fiscalYearRepository.save(fiscalYear);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFiscalYear);
    }

    @GetMapping("/fiscal-years")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get all fiscal years", description = "Retrieves all fiscal years")
    public ResponseEntity<List<FiscalYear>> getAllFiscalYears() {
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<FiscalYear> fiscalYears = fiscalYearRepository.findByTenantIdOrderByYearNumberDesc(tenantId);
        return ResponseEntity.ok(fiscalYears);
    }

    @GetMapping("/fiscal-years/current")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get current fiscal year", description = "Retrieves the current fiscal year")
    public ResponseEntity<FiscalYear> getCurrentFiscalYear() {
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        Optional<FiscalYear> fiscalYear = fiscalYearRepository.findByTenantIdAndIsCurrentTrue(tenantId);
        return fiscalYear.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/fiscal-years/{fiscalYearId}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get fiscal year by ID", description = "Retrieves a fiscal year by its ID")
    public ResponseEntity<FiscalYear> getFiscalYear(
            @Parameter(description = "Fiscal year ID") @PathVariable UUID fiscalYearId) {
        Optional<FiscalYear> fiscalYear = fiscalYearRepository.findById(fiscalYearId);
        return fiscalYear.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/fiscal-years/date/{date}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get fiscal year for date", description = "Retrieves the fiscal year for a specific date")
    public ResponseEntity<FiscalYear> getFiscalYearForDate(
            @Parameter(description = "Date (YYYY-MM-DD)") @PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        Optional<FiscalYear> fiscalYear = fiscalYearRepository.findFiscalYearForDate(tenantId, localDate);
        return fiscalYear.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/fiscal-years/{fiscalYearId}/set-current")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Set fiscal year as current", description = "Sets a fiscal year as the current fiscal year")
    public ResponseEntity<FiscalYear> setFiscalYearAsCurrent(
            @Parameter(description = "Fiscal year ID") @PathVariable UUID fiscalYearId) {
        Optional<FiscalYear> fiscalYearOpt = fiscalYearRepository.findById(fiscalYearId);
        if (fiscalYearOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        FiscalYear fiscalYear = fiscalYearOpt.get();
        
        // Clear current flag from all fiscal years
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<FiscalYear> allFiscalYears = fiscalYearRepository.findByTenantIdOrderByYearNumberDesc(tenantId);
        allFiscalYears.forEach(fy -> fy.setIsCurrent(false));
        fiscalYearRepository.saveAll(allFiscalYears);
        
        // Set current flag
        fiscalYear.setIsCurrent(true);
        FiscalYear updatedFiscalYear = fiscalYearRepository.save(fiscalYear);
        
        return ResponseEntity.ok(updatedFiscalYear);
    }

    @PostMapping("/fiscal-years/{fiscalYearId}/close")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Close fiscal year", description = "Closes a fiscal year")
    public ResponseEntity<FiscalYear> closeFiscalYear(
            @Parameter(description = "Fiscal year ID") @PathVariable UUID fiscalYearId) {
        Optional<FiscalYear> fiscalYearOpt = fiscalYearRepository.findById(fiscalYearId);
        if (fiscalYearOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        FiscalYear fiscalYear = fiscalYearOpt.get();
        fiscalYear.setIsClosed(true);
        fiscalYear.setClosedAt(LocalDate.now());
        // fiscalYear.setClosedBy(getCurrentUser()); // Would need to implement
        
        FiscalYear updatedFiscalYear = fiscalYearRepository.save(fiscalYear);
        return ResponseEntity.ok(updatedFiscalYear);
    }

    // Accounting Period Endpoints
    @PostMapping("/accounting-periods")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Create accounting period", description = "Creates a new accounting period")
    public ResponseEntity<AccountingPeriod> createAccountingPeriod(@Valid @RequestBody AccountingPeriod period) {
        log.info("Creating accounting period: {}", period.getPeriodName());
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        if (accountingPeriodRepository.existsByTenantIdAndFiscalYearIdAndPeriodNumber(
                tenantId, period.getFiscalYearId(), period.getPeriodNumber())) {
            throw new IllegalArgumentException("Period " + period.getPeriodNumber() + 
                " already exists for this fiscal year");
        }
        
        AccountingPeriod savedPeriod = accountingPeriodRepository.save(period);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPeriod);
    }

    @GetMapping("/fiscal-years/{fiscalYearId}/periods")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get accounting periods for fiscal year", description = "Retrieves accounting periods for a fiscal year")
    public ResponseEntity<List<AccountingPeriod>> getAccountingPeriods(
            @Parameter(description = "Fiscal year ID") @PathVariable UUID fiscalYearId) {
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<AccountingPeriod> periods = accountingPeriodRepository.findByTenantIdAndFiscalYearIdOrderByPeriodNumber(
            tenantId, fiscalYearId);
        return ResponseEntity.ok(periods);
    }

    @GetMapping("/accounting-periods/{periodId}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get accounting period by ID", description = "Retrieves an accounting period by its ID")
    public ResponseEntity<AccountingPeriod> getAccountingPeriod(
            @Parameter(description = "Accounting period ID") @PathVariable UUID periodId) {
        Optional<AccountingPeriod> period = accountingPeriodRepository.findById(periodId);
        return period.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/accounting-periods/open")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get open accounting periods", description = "Retrieves all open accounting periods")
    public ResponseEntity<List<AccountingPeriod>> getOpenAccountingPeriods() {
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<AccountingPeriod> periods = accountingPeriodRepository.findByTenantIdAndIsOpenTrue(tenantId);
        return ResponseEntity.ok(periods);
    }

    @GetMapping("/accounting-periods/date/{date}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get accounting period for date", description = "Retrieves the accounting period for a specific date")
    public ResponseEntity<AccountingPeriod> getAccountingPeriodForDate(
            @Parameter(description = "Date (YYYY-MM-DD)") @PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        Optional<AccountingPeriod> period = accountingPeriodRepository.findPeriodForDate(tenantId, localDate);
        return period.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/accounting-periods/{periodId}/close")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Close accounting period", description = "Closes an accounting period")
    public ResponseEntity<AccountingPeriod> closeAccountingPeriod(
            @Parameter(description = "Accounting period ID") @PathVariable UUID periodId) {
        Optional<AccountingPeriod> periodOpt = accountingPeriodRepository.findById(periodId);
        if (periodOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        AccountingPeriod period = periodOpt.get();
        period.setIsClosed(true);
        period.setIsOpen(false);
        period.setClosedAt(LocalDate.now());
        // period.setClosedBy(getCurrentUser()); // Would need to implement
        
        AccountingPeriod updatedPeriod = accountingPeriodRepository.save(period);
        return ResponseEntity.ok(updatedPeriod);
    }

    @PostMapping("/accounting-periods/{periodId}/open")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Open accounting period", description = "Opens a closed accounting period")
    public ResponseEntity<AccountingPeriod> openAccountingPeriod(
            @Parameter(description = "Accounting period ID") @PathVariable UUID periodId) {
        Optional<AccountingPeriod> periodOpt = accountingPeriodRepository.findById(periodId);
        if (periodOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        AccountingPeriod period = periodOpt.get();
        period.setIsClosed(false);
        period.setIsOpen(true);
        
        AccountingPeriod updatedPeriod = accountingPeriodRepository.save(period);
        return ResponseEntity.ok(updatedPeriod);
    }
}
