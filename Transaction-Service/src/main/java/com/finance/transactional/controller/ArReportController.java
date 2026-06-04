package com.finance.transactional.controller;

import com.finance.transactional.dto.ArAgingReportDto;
import com.finance.transactional.service.ArAgingReportService;
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
@RequestMapping("/api/transactional/ar/reports/{tenantId}")
@RequiredArgsConstructor
@Tag(name = "AR Reports", description = "AR aging and receivables analytics")
public class ArReportController {

    private final ArAgingReportService arAgingReportService;

    @GetMapping("/aging")
    @Operation(
            summary = "AR aging report",
            description = "Outstanding receivables by age. Use bucketDays for custom buckets (e.g. 15,45,75,120).")
    public ResponseEntity<ArAgingReportDto> agingReport(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate,
            @RequestParam(required = false) String bucketDays) {

        return ResponseEntity.ok(arAgingReportService.buildReport(tenantId, asOfDate, bucketDays));
    }
}
