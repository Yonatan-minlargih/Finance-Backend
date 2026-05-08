package com.financial.corefinance.client;

import com.financial.corefinance.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "costing-service",
        url = "${services.costing.url:http://localhost:8083/costing-service}",
        configuration = FeignClientConfig.class
)
public interface CostingServiceClient {

    @GetMapping("/api/v1/integration/health")
    String health();
}
