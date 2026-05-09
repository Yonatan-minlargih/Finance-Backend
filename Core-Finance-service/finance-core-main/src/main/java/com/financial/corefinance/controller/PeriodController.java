package com.financial.corefinance.controller;

import com.financial.corefinance.dto.request.AccountingPeriodRequest;
import com.financial.corefinance.dto.request.FiscalYearRequest;
import com.financial.corefinance.dto.request.CalendarDefinitionRequest;
import com.financial.corefinance.dto.response.AccountingPeriodResponse;
import com.financial.corefinance.dto.response.FiscalYearResponse;
import com.financial.corefinance.dto.response.CalendarDefinitionResponse;
import com.financial.corefinance.domain.entity.FiscalYear;
import com.financial.corefinance.domain.entity.AccountingPeriod;
import com.financial.corefinance.domain.entity.CalendarDefinition;
import com.financial.corefinance.service.FiscalYearService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/periods")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fiscal Period Management", description = "APIs for managing fiscal years and accounting periods")
public class PeriodController {

    private final FiscalYearService fiscalYearService;

    // Calendar Definition Endpoints
    @PostMapping("/calendar-definitions")
    @Operation(summary = "Create calendar definition")
    public ResponseEntity<CalendarDefinitionResponse> createCalendarDefinition(@Valid @RequestBody CalendarDefinitionRequest request) {
        CalendarDefinition definition = toCalendarDefinitionEntity(request);
        CalendarDefinition saved = fiscalYearService.createCalendarDefinition(definition);
        return ResponseEntity.status(HttpStatus.CREATED).body(toCalendarDefinitionResponse(saved));
    }

    @GetMapping("/calendar-definitions")
    @Operation(summary = "Get all calendar definitions")
    public ResponseEntity<List<CalendarDefinitionResponse>> getAllCalendarDefinitions() {
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<CalendarDefinitionResponse> definitions = fiscalYearService.getAllCalendarDefinitions(tenantId).stream()
                .map(this::toCalendarDefinitionResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(definitions);
    }

    // Fiscal Year Endpoints
    @PostMapping("/fiscal-years")
    // @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Create fiscal year", description = "Creates a new fiscal year")
    public ResponseEntity<FiscalYearResponse> createFiscalYear(@Valid @RequestBody FiscalYearRequest request) {
        log.info("Creating fiscal year: {}", request.getYearNumber());
        FiscalYear fiscalYear = toFiscalYearEntity(request);
        FiscalYear savedFiscalYear = fiscalYearService.createFiscalYear(fiscalYear);
        return ResponseEntity.status(HttpStatus.CREATED).body(toFiscalYearResponse(savedFiscalYear));
    }

    @GetMapping("/fiscal-years")
    // @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get all fiscal years", description = "Retrieves all fiscal years")
    public ResponseEntity<List<FiscalYearResponse>> getAllFiscalYears() {
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<FiscalYearResponse> fiscalYears = fiscalYearService.getAllFiscalYears(tenantId).stream()
                .map(this::toFiscalYearResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(fiscalYears);
    }

    @GetMapping("/fiscal-years/current")
    // @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get current fiscal year", description = "Retrieves the current fiscal year")
    public ResponseEntity<FiscalYearResponse> getCurrentFiscalYear() {
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        Optional<FiscalYear> fiscalYear = fiscalYearService.getCurrentFiscalYear(tenantId);
        return fiscalYear.map(this::toFiscalYearResponse).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/fiscal-years/{fiscalYearId}")
    // @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get fiscal year by ID", description = "Retrieves a fiscal year by its ID")
    public ResponseEntity<FiscalYearResponse> getFiscalYear(
            @Parameter(description = "Fiscal year ID") @PathVariable UUID fiscalYearId) {
        Optional<FiscalYear> fiscalYear = fiscalYearService.getFiscalYearById(fiscalYearId);
        return fiscalYear.map(this::toFiscalYearResponse).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/fiscal-years/date/{date}")
    // @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get fiscal year for date", description = "Retrieves the fiscal year for a specific date")
    public ResponseEntity<FiscalYearResponse> getFiscalYearForDate(
            @Parameter(description = "Date (YYYY-MM-DD)") @PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        Optional<FiscalYear> fiscalYear = fiscalYearService.getFiscalYearForDate(tenantId, localDate);
        return fiscalYear.map(this::toFiscalYearResponse).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/fiscal-years/{fiscalYearId}/set-current")
    // @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Set fiscal year as current", description = "Sets a fiscal year as the current fiscal year")
    public ResponseEntity<FiscalYearResponse> setFiscalYearAsCurrent(
            @Parameter(description = "Fiscal year ID") @PathVariable UUID fiscalYearId) {
        return ResponseEntity.ok(toFiscalYearResponse(fiscalYearService.setCurrentFiscalYear(fiscalYearId)));
    }

    @PostMapping("/fiscal-years/{fiscalYearId}/generate-periods")
    // @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Generate standard periods", description = "Generates 12 standard monthly periods for a fiscal year")
    public ResponseEntity<List<AccountingPeriodResponse>> generatePeriods(
            @Parameter(description = "Fiscal year ID") @PathVariable UUID fiscalYearId) {
        List<AccountingPeriodResponse> periods = fiscalYearService.generateStandardPeriods(fiscalYearId).stream()
                .map(this::toAccountingPeriodResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(periods);
    }

    @PostMapping("/fiscal-years/{fiscalYearId}/close")
    // @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Close fiscal year", description = "Closes a fiscal year")
    public ResponseEntity<FiscalYearResponse> closeFiscalYear(
            @Parameter(description = "Fiscal year ID") @PathVariable UUID fiscalYearId) {
        String user = "system";
        return ResponseEntity.ok(toFiscalYearResponse(fiscalYearService.closeFiscalYear(fiscalYearId, user)));
    }

    // Accounting Period Endpoints
    @PostMapping("/accounting-periods")
    // @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Create accounting period", description = "Creates a new accounting period")
    public ResponseEntity<AccountingPeriodResponse> createAccountingPeriod(@Valid @RequestBody AccountingPeriodRequest request) {
        log.info("Creating accounting period: {}", request.getPeriodName());
        AccountingPeriod period = toAccountingPeriodEntity(request);

        AccountingPeriod savedPeriod = fiscalYearService.createAccountingPeriod(period);
        return ResponseEntity.status(HttpStatus.CREATED).body(toAccountingPeriodResponse(savedPeriod));
    }

    @GetMapping("/fiscal-years/{fiscalYearId}/periods")
    // @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get accounting periods for fiscal year", description = "Retrieves accounting periods for a fiscal year")
    public ResponseEntity<List<AccountingPeriodResponse>> getAccountingPeriods(
            @Parameter(description = "Fiscal year ID") @PathVariable UUID fiscalYearId) {
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<AccountingPeriodResponse> periods = fiscalYearService.getAccountingPeriodsByFiscalYear(tenantId, fiscalYearId).stream()
                .map(this::toAccountingPeriodResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(periods);
    }

    @GetMapping("/accounting-periods/{periodId}")
    // @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get accounting period by ID", description = "Retrieves an accounting period by its ID")
    public ResponseEntity<AccountingPeriodResponse> getAccountingPeriod(
            @Parameter(description = "Accounting period ID") @PathVariable UUID periodId) {
        Optional<AccountingPeriod> period = fiscalYearService.getAccountingPeriodById(periodId);
        return period.map(this::toAccountingPeriodResponse).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/accounting-periods/open")
    // @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get open accounting periods", description = "Retrieves all open accounting periods")
    public ResponseEntity<List<AccountingPeriodResponse>> getOpenAccountingPeriods() {
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<AccountingPeriodResponse> periods = fiscalYearService.getOpenAccountingPeriods(tenantId).stream()
                .map(this::toAccountingPeriodResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(periods);
    }

    @GetMapping("/accounting-periods/date/{date}")
    // @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get accounting period for date", description = "Retrieves the accounting period for a specific date")
    public ResponseEntity<AccountingPeriodResponse> getAccountingPeriodForDate(
            @Parameter(description = "Date (YYYY-MM-DD)") @PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        Optional<AccountingPeriod> period = fiscalYearService.getAccountingPeriodForDate(tenantId, localDate);
        return period.map(this::toAccountingPeriodResponse).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/accounting-periods/{periodId}/close")
    // @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Close accounting period", description = "Closes an accounting period")
    public ResponseEntity<AccountingPeriodResponse> closeAccountingPeriod(
            @Parameter(description = "Accounting period ID") @PathVariable UUID periodId) {
        return ResponseEntity.ok(toAccountingPeriodResponse(fiscalYearService.closeAccountingPeriod(periodId, "system")));
    }

    @PostMapping("/accounting-periods/{periodId}/open")
    // @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Open accounting period", description = "Opens a closed accounting period")
    public ResponseEntity<AccountingPeriodResponse> openAccountingPeriod(
            @Parameter(description = "Accounting period ID") @PathVariable UUID periodId) {
        return ResponseEntity.ok(toAccountingPeriodResponse(fiscalYearService.openAccountingPeriod(periodId)));
    }

    private FiscalYear toFiscalYearEntity(FiscalYearRequest request) {
        FiscalYear fiscalYear = new FiscalYear();
        fiscalYear.setTenantId(request.getTenantId());
        fiscalYear.setYearNumber(request.getYearNumber());
        fiscalYear.setYearName(request.getYearName());
        fiscalYear.setStartDate(request.getStartDate());
        fiscalYear.setEndDate(request.getEndDate());
        fiscalYear.setCalendarDefinitionId(request.getCalendarDefinitionId());
        fiscalYear.setIsCurrent(request.getIsCurrent());
        fiscalYear.setDescription(request.getDescription());
        return fiscalYear;
    }

    private AccountingPeriod toAccountingPeriodEntity(AccountingPeriodRequest request) {
        AccountingPeriod period = new AccountingPeriod();
        period.setTenantId(request.getTenantId());
        period.setFiscalYearId(request.getFiscalYearId());
        period.setPeriodNumber(request.getPeriodNumber());
        period.setPeriodName(request.getPeriodName());
        period.setStartDate(request.getStartDate());
        period.setEndDate(request.getEndDate());
        period.setIsOpen(request.getIsOpen());
        period.setIsAdjustmentPeriod(request.getIsAdjustmentPeriod());
        period.setDescription(request.getDescription());
        return period;
    }
    private FiscalYearResponse toFiscalYearResponse(FiscalYear fiscalYear) {
        FiscalYearResponse response = new FiscalYearResponse();
        response.setId(fiscalYear.getId());
        response.setTenantId(fiscalYear.getTenantId());
        response.setCreatedAt(fiscalYear.getCreatedAt());
        response.setUpdatedAt(fiscalYear.getUpdatedAt());
        response.setCreatedBy(fiscalYear.getCreatedBy());
        response.setUpdatedBy(fiscalYear.getUpdatedBy());
        response.setVersion(fiscalYear.getVersion());
        response.setYearNumber(fiscalYear.getYearNumber());
        response.setYearName(fiscalYear.getYearName());
        response.setStartDate(fiscalYear.getStartDate());
        response.setEndDate(fiscalYear.getEndDate());
        response.setCalendarDefinitionId(fiscalYear.getCalendarDefinitionId());
        response.setIsCurrent(fiscalYear.getIsCurrent());
        response.setIsClosed(fiscalYear.getIsClosed());
        response.setClosedAt(fiscalYear.getClosedAt());
        response.setClosedBy(fiscalYear.getClosedBy());
        response.setTotalPeriods(fiscalYear.getTotalPeriods());
        response.setDescription(fiscalYear.getDescription());
        return response;
    }

    private CalendarDefinitionResponse toCalendarDefinitionResponse(CalendarDefinition definition) {
        return CalendarDefinitionResponse.builder()
                .id(definition.getId())
                .tenantId(definition.getTenantId())
                .calendarName(definition.getCalendarName())
                .description(definition.getDescription())
                .isDefault(definition.getIsDefault())
                .periodType(definition.getPeriodType())
                .yearStartMonth(definition.getYearStartMonth())
                .yearStartDay(definition.getYearStartDay())
                .effectiveFrom(definition.getEffectiveFrom())
                .build();
    }

    private CalendarDefinition toCalendarDefinitionEntity(CalendarDefinitionRequest request) {
        return CalendarDefinition.builder()
                .tenantId(request.getTenantId())
                .calendarName(request.getCalendarName())
                .description(request.getDescription())
                .isDefault(request.getIsDefault())
                .periodType(request.getPeriodType())
                .yearStartMonth(request.getYearStartMonth())
                .yearStartDay(request.getYearStartDay())
                .effectiveFrom(request.getEffectiveFrom())
                .build();
    }

    private AccountingPeriodResponse toAccountingPeriodResponse(AccountingPeriod period) {
        AccountingPeriodResponse response = new AccountingPeriodResponse();
        response.setId(period.getId());
        response.setTenantId(period.getTenantId());
        response.setCreatedAt(period.getCreatedAt());
        response.setUpdatedAt(period.getUpdatedAt());
        response.setCreatedBy(period.getCreatedBy());
        response.setUpdatedBy(period.getUpdatedBy());
        response.setVersion(period.getVersion());
        response.setFiscalYearId(period.getFiscalYearId());
        response.setPeriodNumber(period.getPeriodNumber());
        response.setPeriodName(period.getPeriodName());
        response.setStartDate(period.getStartDate());
        response.setEndDate(period.getEndDate());
        response.setIsOpen(period.getIsOpen());
        response.setIsClosed(period.getIsClosed());
        response.setClosedAt(period.getClosedAt());
        response.setClosedBy(period.getClosedBy());
        response.setIsAdjustmentPeriod(period.getIsAdjustmentPeriod());
        response.setDescription(period.getDescription());
        return response;
    }
}