package com.financial.corefinance.controller;

import com.financial.corefinance.domain.base.TenantContext;
import com.financial.corefinance.dto.response.TrialBalanceReportResponse;
import com.financial.corefinance.service.TrialBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "GL Reports", description = "Trial balance and GL analytical reports")
public class GlReportController {

    private final TrialBalanceService trialBalanceService;

    @GetMapping("/trial-balance")
    @Operation(
            summary = "Trial balance",
            description = "Posted GL balances by account; highlights AP payable (2100) for subledger reconciliation")
    public ResponseEntity<TrialBalanceReportResponse> trialBalance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        String tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(trialBalanceService.buildTrialBalance(tenantId, asOfDate));
    }
}
