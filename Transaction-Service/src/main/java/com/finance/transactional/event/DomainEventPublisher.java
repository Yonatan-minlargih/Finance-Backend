package com.finance.transactional.event;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final TransactionalEventProducer transactionalEventProducer;

    public void publish(String eventName, Object payload) {
        switch (eventName) {
            case "invoice-approved" -> transactionalEventProducer.publishInvoiceApproved(payload);
            case "payment-posted" -> transactionalEventProducer.publishPaymentPosted(payload);
            default -> log.warn("Unknown transactional event '{}'", eventName);
        }
    }
}
