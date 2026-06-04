package com.finance.transactional.client;

import com.finance.transactional.config.FeignClientConfig;
import com.finance.transactional.dto.corefinance.CoreFinanceJournalReverseResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "core-finance-journal-client",
        url = "${core-finance.url:http://localhost:8084}",
        configuration = FeignClientConfig.class)
public interface CoreFinanceJournalClient {

    @PostMapping("/api/v1/integration/journals/{journalId}/reverse")
    CoreFinanceJournalReverseResponse reverseJournal(
            @PathVariable("journalId") UUID journalId, @RequestParam("reversalReason") String reversalReason);
}
