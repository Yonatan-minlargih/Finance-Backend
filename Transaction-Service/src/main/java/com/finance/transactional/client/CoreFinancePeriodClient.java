package com.finance.transactional.client;

import com.finance.transactional.config.FeignClientConfig;
import com.finance.transactional.dto.corefinance.AccountingPeriodLookupDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "core-finance-period-client",
        url = "${core-finance.url:http://localhost:8084}",
        configuration = FeignClientConfig.class
)
public interface CoreFinancePeriodClient {

    @GetMapping("/api/v1/periods/accounting-periods/date/{date}")
    AccountingPeriodLookupDto getAccountingPeriodForDate(
            @PathVariable("date") String date,
            @RequestHeader("X-Tenant-ID") String tenantId);
}
