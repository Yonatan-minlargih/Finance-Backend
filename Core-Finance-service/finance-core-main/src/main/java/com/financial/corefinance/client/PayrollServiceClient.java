package com.financial.corefinance.client;

import com.financial.corefinance.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "payroll-service",
        url = "${services.payroll.url:http://localhost:8084/payroll-service}",
        configuration = FeignClientConfig.class
)
public interface PayrollServiceClient {

    @GetMapping("/api/v1/integration/health")
    String health();
}
