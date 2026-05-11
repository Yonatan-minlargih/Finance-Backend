package com.example.cost_service.client;

import com.example.cost_service.config.FeignClientConfig;
import com.example.cost_service.dto.clientDto.PeriodDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "core-finance-service", url = "${core-finance-service.url:http://core-finance-service:8080}", path = "/api/v1/periods", configuration = FeignClientConfig.class)
public interface PeriodServiceClient {

    @GetMapping("/accounting-periods/{id}")
    PeriodDto getPeriodById(@PathVariable UUID id);
}
