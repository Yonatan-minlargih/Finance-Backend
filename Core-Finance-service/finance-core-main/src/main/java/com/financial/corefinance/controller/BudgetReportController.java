package com.financial.corefinance.controller;

import com.financial.corefinance.domain.entity.BudgetChange;
import com.financial.corefinance.domain.entity.BudgetLine;
import com.financial.corefinance.domain.entity.BudgetLine.LineCategory;
import com.financial.corefinance.domain.base.TenantContext;
import com.financial.corefinance.dto.request.BudgetForecastRequest;
import com.financial.corefinance.dto.request.BudgetTransferRequest;
import com.financial.corefinance.dto.response.BudgetForecastResponse;
import com.financial.corefinance.dto.response.BudgetMultiYearRowResponse;
import com.financial.corefinance.dto.response.BudgetReportResponse;
import com.financial.corefinance.service.BudgetMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/budget-reports")
@RequiredArgsConstructor
@Tag(name = "Budget Reports", description = "Budget vs actual, inquiry, multi-year, forecasts, transfers")
public class BudgetReportController {

    private final BudgetMonitoringService budgetMonitoringService;

    @GetMapping("/vs-actual")
    @Operation(summary = "Budget vs actual", description = "Budgeted, actual, and variance at department or account level")
    public ResponseEntity<BudgetReportResponse> budgetVsActual(
            @RequestParam UUID budgetId,
            @RequestParam(required = false) UUID budgetVersionId,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate,
            @RequestParam(defaultValue = "ACCOUNT") String groupBy) {
        return ResponseEntity.ok(budgetMonitoringService.getBudgetVsActual(
                budgetId, budgetVersionId, departmentId, accountId, asOfDate, groupBy));
    }

    @GetMapping("/inquiry")
    @Operation(summary = "Mid-year budget inquiry", description = "Actuals through as-of date vs full-year budget")
    public ResponseEntity<BudgetReportResponse> midYearInquiry(
            @RequestParam UUID budgetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate,
            @RequestParam(required = false) UUID departmentId) {
        return ResponseEntity.ok(budgetMonitoringService.getMidYearInquiry(budgetId, asOfDate, departmentId));
    }

    @GetMapping("/multi-year")
    @Operation(summary = "Multi-period budget display", description = "Compare budget and actual across multiple fiscal years")
    public ResponseEntity<List<BudgetMultiYearRowResponse>> multiYear(
            @RequestParam String fiscalYearIds,
            @RequestParam(required = false) UUID departmentId) {
        List<UUID> ids = Arrays.stream(fiscalYearIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(UUID::fromString)
                .collect(Collectors.toList());
        return ResponseEntity.ok(budgetMonitoringService.getMultiYearReport(ids, departmentId));
    }

    @PostMapping("/transfer")
    @Operation(summary = "Budget transfer", description = "Transfer amount from one budget line to another with audit trail")
    public ResponseEntity<List<BudgetChange>> transfer(@Valid @RequestBody BudgetTransferRequest request) {
        String user = TenantContext.getCurrentTenant() + "-user";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetMonitoringService.createBudgetTransfer(request, user));
    }

    @GetMapping("/forecasts")
    @Operation(summary = "List revenue/expenditure forecasts")
    public ResponseEntity<List<BudgetForecastResponse>> listForecasts(
            @RequestParam UUID fiscalYearId,
            @RequestParam(required = false) LineCategory category) {
        return ResponseEntity.ok(budgetMonitoringService.listForecasts(fiscalYearId, category));
    }

    @PostMapping("/forecasts")
    @Operation(summary = "Save forecast line", description = "Revenue or expenditure forecast by period; prior-year actual optional")
    public ResponseEntity<BudgetForecastResponse> saveForecast(@Valid @RequestBody BudgetForecastRequest request) {
        BudgetLine saved = budgetMonitoringService.saveForecast(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BudgetForecastResponse.builder()
                .id(saved.getId())
                .fiscalYearId(request.getFiscalYearId())
                .departmentId(saved.getDepartmentId())
                .accountId(saved.getAccountId())
                .lineCategory(saved.getLineCategory())
                .periodNumber(saved.getPeriodNumber())
                .forecastAmount(saved.getBudgetAmount())
                .priorYearActualAmount(saved.getPriorYearActualAmount())
                .notes(saved.getNotes())
                .build());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh budget actuals from GL", description = "Recalculates actual amounts from all posted journals")
    public ResponseEntity<Void> refreshActuals(
            @RequestParam UUID budgetId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        budgetMonitoringService.refreshBudgetActuals(budgetId, asOfDate);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/prior-year-actual")
    @Operation(summary = "Prior year actual reference", description = "GL actual for prior fiscal year (expenditure reference)")
    public ResponseEntity<BigDecimal> priorYearActual(
            @RequestParam UUID fiscalYearId,
            @RequestParam UUID accountId,
            @RequestParam(required = false) UUID departmentId) {
        return ResponseEntity.ok(budgetMonitoringService.getPriorYearActual(fiscalYearId, accountId, departmentId));
    }
}
