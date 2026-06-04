package com.finance.transactional.controller;

import com.finance.transactional.dto.ApAgingReportDto;
import com.finance.transactional.dto.ApVendorStatementDto;
import com.finance.transactional.service.ApAgingReportService;
import com.finance.transactional.service.ApVendorStatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactional/ap/reports/{tenantId}")
@RequiredArgsConstructor
@Tag(name = "AP Reports", description = "AP aging, vendor statements, and subledger reports")
public class ApReportController {

    private final ApAgingReportService apAgingReportService;
    private final ApVendorStatementService apVendorStatementService;

    @GetMapping("/aging")
    @Operation(summary = "AP aging report", description = "Outstanding payables bucketed by age (30/60/90 day buckets)")
    public ResponseEntity<ApAgingReportDto> apAgingReport(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        return ResponseEntity.ok(apAgingReportService.buildAgingReport(tenantId, asOfDate));
    }

    @GetMapping("/vendor-statement/{vendorId}")
    @Operation(summary = "Vendor statement", description = "Invoices, payments, credits, and outstanding balance for a vendor")
    public ResponseEntity<ApVendorStatementDto> vendorStatement(
            @PathVariable UUID tenantId,
            @PathVariable UUID vendorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        return ResponseEntity.ok(
                apVendorStatementService.buildStatement(tenantId, vendorId, fromDate, toDate, asOfDate));
    }
}
