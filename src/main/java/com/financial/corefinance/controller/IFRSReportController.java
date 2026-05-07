package com.financial.corefinance.controller;

import com.financial.corefinance.domain.entity.FinancialReport;
import com.financial.corefinance.repository.FinancialReportRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ifrs-reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "IFRS Reporting", description = "APIs for generating and managing IFRS financial reports")
public class IFRSReportController {

    private final FinancialReportRepository financialReportRepository;

    @PostMapping
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('REPORT_MANAGER')")
    @Operation(summary = "Create financial report", description = "Creates a new financial report")
    public ResponseEntity<FinancialReport> createFinancialReport(@Valid @RequestBody FinancialReport report) {
        log.info("Creating financial report: {}", report.getReportName());
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        if (financialReportRepository.existsByTenantIdAndFiscalYearIdAndReportTypeAndReportName(
                tenantId, report.getFiscalYearId(), report.getReportType(), report.getReportName())) {
            throw new IllegalArgumentException("Financial report with name " + report.getReportName() + 
                " already exists for this fiscal year and report type");
        }
        
        report.setStatus(FinancialReport.ReportStatus.DRAFT);
        FinancialReport savedReport = financialReportRepository.save(report);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReport);
    }

    @GetMapping("/{reportId}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('REPORT_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get financial report by ID", description = "Retrieves a financial report by its ID")
    public ResponseEntity<FinancialReport> getFinancialReport(
            @Parameter(description = "Report ID") @PathVariable UUID reportId) {
        log.info("Retrieving financial report with ID: {}", reportId);
        
        Optional<FinancialReport> report = financialReportRepository.findById(reportId);
        return report.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('REPORT_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get all financial reports", description = "Retrieves a paginated list of financial reports")
    public ResponseEntity<Page<FinancialReport>> getAllFinancialReports(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "reportName") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        Page<FinancialReport> reports = financialReportRepository.findByTenantId(tenantId, pageable);
        
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/fiscal-year/{fiscalYearId}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('REPORT_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get reports by fiscal year", description = "Retrieves financial reports for a specific fiscal year")
    public ResponseEntity<List<FinancialReport>> getReportsByFiscalYear(
            @Parameter(description = "Fiscal year ID") @PathVariable UUID fiscalYearId) {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<FinancialReport> reports = financialReportRepository.findByTenantIdAndFiscalYearId(tenantId, fiscalYearId);
        
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/type/{reportType}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('REPORT_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get reports by type", description = "Retrieves financial reports filtered by type")
    public ResponseEntity<List<FinancialReport>> getReportsByType(
            @Parameter(description = "Report type") @PathVariable FinancialReport.ReportType reportType) {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<FinancialReport> reports = financialReportRepository.findByTenantIdAndReportType(tenantId, reportType);
        
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('REPORT_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get reports by status", description = "Retrieves financial reports filtered by status")
    public ResponseEntity<List<FinancialReport>> getReportsByStatus(
            @Parameter(description = "Report status") @PathVariable FinancialReport.ReportStatus status) {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<FinancialReport> reports = financialReportRepository.findByTenantIdAndStatus(tenantId, status);
        
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('REPORT_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Search financial reports", description = "Searches financial reports by name or description")
    public ResponseEntity<Page<FinancialReport>> searchFinancialReports(
            @Parameter(description = "Search term") @RequestParam String search,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "reportName"));
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        Page<FinancialReport> reports = financialReportRepository.searchReports(tenantId, search, pageable);
        
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/fiscal-year/{fiscalYearId}/type/{reportType}/latest")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('REPORT_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get latest report by type", description = "Retrieves the latest report of a specific type for a fiscal year")
    public ResponseEntity<List<FinancialReport>> getLatestReportsByType(
            @Parameter(description = "Fiscal year ID") @PathVariable UUID fiscalYearId,
            @Parameter(description = "Report type") @PathVariable FinancialReport.ReportType reportType) {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<FinancialReport> reports = financialReportRepository.findLatestReportsByType(tenantId, fiscalYearId, reportType);
        
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/as-of-date/{date}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('REPORT_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get reports as of date", description = "Retrieves financial reports as of a specific date")
    public ResponseEntity<List<FinancialReport>> getReportsAsOfDate(
            @Parameter(description = "Date (YYYY-MM-DD)") @PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<FinancialReport> reports = financialReportRepository.findReportsAsOfDate(tenantId, localDate);
        
        return ResponseEntity.ok(reports);
    }

    // IFRS Specific Report Generation Endpoints
    @PostMapping("/generate/statement-of-financial-position")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('REPORT_MANAGER')")
    @Operation(summary = "Generate Statement of Financial Position", description = "Generates an IFRS Statement of Financial Position (Balance Sheet)")
    public ResponseEntity<FinancialReport> generateStatementOfFinancialPosition(
            @Parameter(description = "Fiscal year ID") @RequestParam UUID fiscalYearId,
            @Parameter(description = "As of date") @RequestParam String asOfDate,
            @Parameter(description = "Report name") @RequestParam(required = false) String reportName) {
        
        LocalDate localDate = LocalDate.parse(asOfDate);
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        
        FinancialReport report = FinancialReport.builder()
            .tenantId(tenantId)
            .fiscalYearId(fiscalYearId)
            .reportName(reportName != null ? reportName : "Statement of Financial Position")
            .reportType(FinancialReport.ReportType.STATEMENT_OF_FINANCIAL_POSITION)
            .asOfDate(localDate)
            .reportDate(LocalDate.now())
            .currencyCode("USD")
            .status(FinancialReport.ReportStatus.GENERATING)
            .build();
        
        FinancialReport savedReport = financialReportRepository.save(report);
        
        // Here you would implement the actual report generation logic
        // This would involve querying accounts, calculating balances, and creating report lines
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReport);
    }

    @PostMapping("/generate/profit-loss")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('REPORT_MANAGER')")
    @Operation(summary = "Generate Profit and Loss Statement", description = "Generates an IFRS Profit and Loss and Other Comprehensive Income Statement")
    public ResponseEntity<FinancialReport> generateProfitLossStatement(
            @Parameter(description = "Fiscal year ID") @RequestParam UUID fiscalYearId,
            @Parameter(description = "Period number") @RequestParam(required = false) Integer periodNumber,
            @Parameter(description = "Report name") @RequestParam(required = false) String reportName) {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        
        FinancialReport report = FinancialReport.builder()
            .tenantId(tenantId)
            .fiscalYearId(fiscalYearId)
            .reportName(reportName != null ? reportName : "Profit and Loss and OCI")
            .reportType(FinancialReport.ReportType.PROFIT_LOSS_AND_OCI)
            .periodNumber(periodNumber)
            .reportDate(LocalDate.now())
            .currencyCode("USD")
            .status(FinancialReport.ReportStatus.GENERATING)
            .build();
        
        FinancialReport savedReport = financialReportRepository.save(report);
        
        // Implement actual P&L generation logic here
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReport);
    }

    @PostMapping("/generate/cash-flow")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('REPORT_MANAGER')")
    @Operation(summary = "Generate Cash Flow Statement", description = "Generates an IFRS Cash Flow Statement")
    public ResponseEntity<FinancialReport> generateCashFlowStatement(
            @Parameter(description = "Fiscal year ID") @RequestParam UUID fiscalYearId,
            @Parameter(description = "Period number") @RequestParam(required = false) Integer periodNumber,
            @Parameter(description = "Report name") @RequestParam(required = false) String reportName) {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        
        FinancialReport report = FinancialReport.builder()
            .tenantId(tenantId)
            .fiscalYearId(fiscalYearId)
            .reportName(reportName != null ? reportName : "Cash Flow Statement")
            .reportType(FinancialReport.ReportType.CASH_FLOW_STATEMENT)
            .periodNumber(periodNumber)
            .reportDate(LocalDate.now())
            .currencyCode("USD")
            .status(FinancialReport.ReportStatus.GENERATING)
            .build();
        
        FinancialReport savedReport = financialReportRepository.save(report);
        
        // Implement actual cash flow generation logic here
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReport);
    }

    @PostMapping("/generate/changes-in-equity")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('REPORT_MANAGER')")
    @Operation(summary = "Generate Statement of Changes in Equity", description = "Generates an IFRS Statement of Changes in Equity")
    public ResponseEntity<FinancialReport> generateChangesInEquityStatement(
            @Parameter(description = "Fiscal year ID") @RequestParam UUID fiscalYearId,
            @Parameter(description = "Report name") @RequestParam(required = false) String reportName) {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        
        FinancialReport report = FinancialReport.builder()
            .tenantId(tenantId)
            .fiscalYearId(fiscalYearId)
            .reportName(reportName != null ? reportName : "Statement of Changes in Equity")
            .reportType(FinancialReport.ReportType.STATEMENT_OF_CHANGES_IN_EQUITY)
            .reportDate(LocalDate.now())
            .currencyCode("USD")
            .status(FinancialReport.ReportStatus.GENERATING)
            .build();
        
        FinancialReport savedReport = financialReportRepository.save(report);
        
        // Implement actual changes in equity generation logic here
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReport);
    }

    @PostMapping("/{reportId}/approve")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Approve financial report", description = "Approves a financial report")
    public ResponseEntity<FinancialReport> approveFinancialReport(
            @Parameter(description = "Report ID") @PathVariable UUID reportId) {
        Optional<FinancialReport> reportOpt = financialReportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        FinancialReport report = reportOpt.get();
        report.setStatus(FinancialReport.ReportStatus.APPROVED);
        report.setApprovedAt(LocalDate.now());
        // report.setApprovedBy(getCurrentUser()); // Would need to implement
        
        FinancialReport updatedReport = financialReportRepository.save(report);
        return ResponseEntity.ok(updatedReport);
    }

    @PostMapping("/{reportId}/publish")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Publish financial report", description = "Publishes a financial report")
    public ResponseEntity<FinancialReport> publishFinancialReport(
            @Parameter(description = "Report ID") @PathVariable UUID reportId) {
        Optional<FinancialReport> reportOpt = financialReportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        FinancialReport report = reportOpt.get();
        report.setStatus(FinancialReport.ReportStatus.PUBLISHED);
        report.setPublishedAt(LocalDate.now());
        // report.setPublishedBy(getCurrentUser()); // Would need to implement
        
        FinancialReport updatedReport = financialReportRepository.save(report);
        return ResponseEntity.ok(updatedReport);
    }

    @DeleteMapping("/{reportId}")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Delete financial report", description = "Deletes a financial report")
    public ResponseEntity<Void> deleteFinancialReport(
            @Parameter(description = "Report ID") @PathVariable UUID reportId) {
        log.info("Deleting financial report with ID: {}", reportId);
        
        if (!financialReportRepository.existsById(reportId)) {
            return ResponseEntity.notFound().build();
        }
        
        financialReportRepository.deleteById(reportId);
        return ResponseEntity.noContent().build();
    }
}
