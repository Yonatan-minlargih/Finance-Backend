package com.financial.corefinance.config;

import com.financial.corefinance.service.IntegrationGlAccountService;
import com.financial.corefinance.service.IntegrationGlAccountService.SeedResult;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class IntegrationChartOfAccountsSeeder implements ApplicationRunner {

    private final IntegrationGlAccountProperties properties;
    private final IntegrationGlAccountService integrationGlAccountService;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled() || !properties.isSeedOnStartup()) {
            return;
        }
        List<String> tenants = parseTenants(properties.getSeedTenants());
        if (tenants.isEmpty()) {
            log.info("Integration COA seed skipped: no integration.gl-accounts.seed-tenants configured");
            return;
        }
        for (String tenantId : tenants) {
            try {
                List<SeedResult> results = integrationGlAccountService.seedIntegrationChart(tenantId);
                long created = results.stream().filter(r -> "CREATED".equals(r.status())).count();
                long reactivated = results.stream().filter(r -> "REACTIVATED".equals(r.status())).count();
                long conflicts = results.stream().filter(r -> "CONFLICT".equals(r.status())).count();
                log.info(
                        "Integration COA seed for tenant {}: {} created, {} reactivated, {} conflicts",
                        tenantId,
                        created,
                        reactivated,
                        conflicts);
                results.stream()
                        .filter(r -> "CONFLICT".equals(r.status()))
                        .forEach(r -> log.warn("COA seed conflict {}: {}", r.accountCode(), r.message()));
            } catch (Exception ex) {
                log.error("Integration COA seed failed for tenant {}", tenantId, ex);
            }
        }
    }

    private List<String> parseTenants(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
