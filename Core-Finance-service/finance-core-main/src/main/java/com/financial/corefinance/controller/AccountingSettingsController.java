package com.financial.corefinance.controller;

import com.financial.corefinance.domain.entity.TenantAccountingSettings;
import com.financial.corefinance.dto.response.YearEndCloseResult;
import com.financial.corefinance.repository.TenantAccountingSettingsRepository;
import com.financial.corefinance.service.YearEndClosingService;
import com.financial.corefinance.domain.entity.JournalHeader;
import com.financial.corefinance.domain.base.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Slf4j
public class AccountingSettingsController {

    private final TenantAccountingSettingsRepository settingsRepository;
    private final YearEndClosingService yearEndClosingService;

    @GetMapping("/accounting")
    public ResponseEntity<TenantAccountingSettings> getSettings() {
        String tenantId = TenantContext.getCurrentTenant();
        TenantAccountingSettings settings = settingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> {
                    TenantAccountingSettings defaults = TenantAccountingSettings.builder()
                            .tenantId(tenantId)
                            .build();
                    return settingsRepository.save(defaults);
                });
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/accounting")
    public ResponseEntity<TenantAccountingSettings> updateSettings(@RequestBody TenantAccountingSettings request) {
        String tenantId = TenantContext.getCurrentTenant();
        TenantAccountingSettings settings = settingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> {
                    TenantAccountingSettings defaults = TenantAccountingSettings.builder()
                            .tenantId(tenantId)
                            .build();
                    return settingsRepository.save(defaults);
                });

        if (request.getFiscalYearStartMonth() != null) {
            settings.setFiscalYearStartMonth(request.getFiscalYearStartMonth());
        }
        if (request.getRetainedEarningsAccountCode() != null) {
            String code = request.getRetainedEarningsAccountCode().trim();
            settings.setRetainedEarningsAccountCode(code.isEmpty() ? null : code);
        }
        if (request.getIfrsCompliance() != null) {
            settings.setIfrsCompliance(request.getIfrsCompliance());
        }
        if (request.getGaapAdaptation() != null) {
            settings.setGaapAdaptation(request.getGaapAdaptation());
        }
        if (request.getLocalAudit() != null) {
            settings.setLocalAudit(request.getLocalAudit());
        }
        if (request.getPeriodClosingType() != null) {
            settings.setPeriodClosingType(request.getPeriodClosingType());
        }
        if (request.getBaseCurrency() != null) {
            settings.setBaseCurrency(request.getBaseCurrency());
        }

        TenantAccountingSettings saved = settingsRepository.save(settings);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/year-end-close/{fiscalYearId}")
    public ResponseEntity<?> yearEndClose(@PathVariable UUID fiscalYearId) {
        String tenantId = TenantContext.getCurrentTenant();
        try {
            YearEndCloseResult result = yearEndClosingService.closeFiscalYear(fiscalYearId, tenantId);
            if (result.getClosingJournal() == null) {
                return ResponseEntity.ok(Map.of(
                        "message",
                        "Fiscal year marked closed. No income/expense balances were found in this year "
                                + "(check account types REVENUE/EXPENSE or IFRS revenue/expense categories, and posted journals).",
                        "nominalAccountsClosed", result.getNominalAccountsClosed(),
                        "netIncome", result.getNetIncome()
                ));
            }
            JournalHeader closingJournal = result.getClosingJournal();
            return ResponseEntity.ok(Map.of(
                    "message",
                    "Year-end close posted. Income/expense accounts cleared to retained earnings; "
                            + "balance sheet accounts (cash, receivables, etc.) are unchanged.",
                    "closingJournalId", closingJournal.getId(),
                    "closingJournalNumber", closingJournal.getJournalNumber(),
                    "nominalAccountsClosed", result.getNominalAccountsClosed(),
                    "netIncome", result.getNetIncome()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/year-end-reopen/{fiscalYearId}")
    public ResponseEntity<?> yearEndReopen(@PathVariable UUID fiscalYearId) {
        String tenantId = TenantContext.getCurrentTenant();
        try {
            JournalHeader reversalJournal = yearEndClosingService.reopenFiscalYear(fiscalYearId, tenantId);
            if (reversalJournal == null) {
                return ResponseEntity.ok(Map.of("message", "Fiscal year reopened (no closing journals found to reverse)."));
            }
            return ResponseEntity.ok(Map.of(
                    "message", "Fiscal year reopened successfully. Closing entries reversed.",
                    "reversalJournalId", reversalJournal.getId(),
                    "reversalJournalNumber", reversalJournal.getJournalNumber()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
