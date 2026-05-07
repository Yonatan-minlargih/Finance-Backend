package com.finance.transactional.client;

import com.finance.transactional.model.ap.Invoice;
import com.finance.transactional.utility.CoreFinanceJournalPayloadFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoreFinanceRestClient implements CoreFinanceClient {

    private final RestTemplate restTemplate;
    private final CoreFinanceJournalPayloadFactory payloadFactory;

    @Value("${core-finance.url}")
    private String coreFinanceUrl;

    @Value("${core-finance.enabled:false}")
    private boolean coreFinanceEnabled;

    @Override
    public void postInvoiceApprovalJournal(Invoice invoice) {
        if (!coreFinanceEnabled) {
            log.info("CoreFinance integration is disabled. Skipping journal post for Invoice ID {}", invoice.getId());
            return;
        }

        String url = coreFinanceUrl + "/api/journals/post";
        log.info("Posting journal to CoreFinance: {}", url);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(payloadFactory.fromInvoiceApproval(invoice)),
                    new ParameterizedTypeReference<String>() {}
            );

            log.info("Response from CoreFinance: {}", response.getBody());
        } catch (Exception e) {
            log.error("Failed to post journal to CoreFinance for Invoice ID {}: {}", invoice.getId(), e.getMessage());
            throw new RuntimeException("Integration with CoreFinance failed. Transaction rolled back.", e);
        }
    }
}
