package com.finance.transactional.client;

import com.finance.transactional.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

/**
 * Feign client for calling the Core-Finance Numbering Series endpoint.
 * Used to automatically generate document control numbers for AP invoices,
 * AR invoices, payments, and receipts (SRS FR_1.4).
 */
@FeignClient(
        name = "numbering-series-client",
        url = "${core-finance.url:http://localhost:8084}",
        configuration = FeignClientConfig.class
)
public interface NumberingSeriesClient {

    @PostMapping("/api/v1/numbering-series/next/{seriesCode}")
    Map<String, String> getNextNumber(@PathVariable("seriesCode") String seriesCode);
}
