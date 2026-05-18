package com.example.payroll_service.service;

import com.example.payroll_service.model.PayrollSettings;
import com.example.payroll_service.repository.PayrollSettingsRepository;
import com.example.payroll_service.utility.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollSettingsService {

    private final PayrollSettingsRepository payrollSettingsRepository;
    private final ValidationUtil validationUtil;

    @Transactional(readOnly = true)
    public PayrollSettings getSettings(UUID tenantId) {
        validationUtil.validateTenantAccess(tenantId);

        return payrollSettingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> {
                    log.info("Lazy-initializing default Payroll Settings for tenant: {}", tenantId);
                    PayrollSettings defaultSettings = new PayrollSettings();
                    defaultSettings.setTenantId(tenantId);
                    defaultSettings.setCreatedBy("SYSTEM");
                    defaultSettings.setCreatedByUsername("SYSTEM");
                    // We don't auto-save inside the readOnly transaction to be perfectly safe, 
                    // we will save on demand or return a safe default instance for standard read operations.
                    return defaultSettings;
                });
    }

    @Transactional
    public PayrollSettings updateSettings(UUID tenantId, PayrollSettings request, String username) {
        validationUtil.validateTenantAccess(tenantId);

        PayrollSettings settings = payrollSettingsRepository.findByTenantId(tenantId)
                .orElseGet(() -> {
                    PayrollSettings newSettings = new PayrollSettings();
                    newSettings.setTenantId(tenantId);
                    newSettings.setCreatedBy(username);
                    newSettings.setCreatedByUsername(username);
                    return newSettings;
                });

        if (request.getPensionRate() != null) {
            settings.setPensionRate(request.getPensionRate());
        }
        if (request.getOvertimeNormalRate() != null) {
            settings.setOvertimeNormalRate(request.getOvertimeNormalRate());
        }
        if (request.getOvertimeNightRate() != null) {
            settings.setOvertimeNightRate(request.getOvertimeNightRate());
        }
        if (request.getOvertimeHolidayRate() != null) {
            settings.setOvertimeHolidayRate(request.getOvertimeHolidayRate());
        }
        if (request.getTaxBandsJson() != null) {
            settings.setTaxBandsJson(request.getTaxBandsJson());
        }

        settings.setUpdatedBy(username);
        settings.setUpdatedByUsername(username);

        return payrollSettingsRepository.save(settings);
    }
}
