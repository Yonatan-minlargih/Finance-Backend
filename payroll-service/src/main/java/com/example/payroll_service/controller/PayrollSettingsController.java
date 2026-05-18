package com.example.payroll_service.controller;

import com.example.payroll_service.model.PayrollSettings;
import com.example.payroll_service.service.PayrollSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payroll/settings")
@RequiredArgsConstructor
@Slf4j
public class PayrollSettingsController {

    private final PayrollSettingsService payrollSettingsService;

    @GetMapping
    public ResponseEntity<PayrollSettings> getSettings(
            @RequestHeader("X-Tenant-ID") UUID tenantId) {
        log.info("Fetching payroll settings for tenant: {}", tenantId);
        return ResponseEntity.ok(payrollSettingsService.getSettings(tenantId));
    }

    @PutMapping
    public ResponseEntity<PayrollSettings> updateSettings(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestBody PayrollSettings settings,
            @AuthenticationPrincipal Jwt jwt) {
        String username = jwt != null ? jwt.getClaimAsString("preferred_username") : "anonymous";
        log.info("Updating payroll settings for tenant: {} by user: {}", tenantId, username);
        return ResponseEntity.ok(payrollSettingsService.updateSettings(tenantId, settings, username));
    }
}
