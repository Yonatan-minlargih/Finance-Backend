package com.financial.corefinance.client;

import com.financial.corefinance.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "transactional-service",
        url = "${services.transactional.url:http://localhost:8082/transactional-service}",
        configuration = FeignClientConfig.class
)
public interface TransactionalServiceClient {

    @GetMapping("/api/v1/integration/health")
    String health();
}
