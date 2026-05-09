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
            case "asset-transaction-created" -> transactionalEventProducer.publishAssetTransactionCreated(payload);
            case "bank-transaction-created" -> transactionalEventProducer.publishBankTransactionCreated(payload);
            case "bank-transaction-updated" -> transactionalEventProducer.publishBankTransactionCreated(payload); // We can reuse the same producer method or add a new one
            case "sales-invoice-created" -> transactionalEventProducer.publishSalesInvoiceCreated(payload);
            case "receipt-created" -> transactionalEventProducer.publishReceiptCreated(payload);
            case "purchase-order-created" -> transactionalEventProducer.publishPurchaseOrderCreated(payload);
            case "vendor-created" -> transactionalEventProducer.publishVendorCreated(payload);
            case "customer-created" -> transactionalEventProducer.publishCustomerCreated(payload);
            case "fixed-asset-created" -> transactionalEventProducer.publishFixedAssetCreated(payload);
            default -> log.warn("Unknown transactional event '{}'", eventName);
        }
    }
}
