package com.financial.corefinance.service;

import com.financial.corefinance.client.CostingServiceClient;
import com.financial.corefinance.client.PayrollServiceClient;
import com.financial.corefinance.client.TransactionalServiceClient;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinanceDependencyService {

    private final TransactionalServiceClient transactionalServiceClient;
    private final CostingServiceClient costingServiceClient;
    private final PayrollServiceClient payrollServiceClient;

    public Map<String, String> dependenciesHealth() {
        Map<String, String> health = new LinkedHashMap<>();
        health.put("transactional-service", callHealth(() -> transactionalServiceClient.health()));
        health.put("costing-service", callHealth(() -> costingServiceClient.health()));
        health.put("payroll-service", callHealth(() -> payrollServiceClient.health()));
        return health;
    }

    private String callHealth(HealthCall call) {
        try {
            return call.execute();
        } catch (Exception ex) {
            return "UNREACHABLE: " + ex.getMessage();
        }
    }

    @FunctionalInterface
    private interface HealthCall {
        String execute();
    }
}
