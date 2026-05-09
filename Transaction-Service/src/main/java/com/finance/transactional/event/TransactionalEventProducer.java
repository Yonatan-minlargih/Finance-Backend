package com.finance.transactional.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionalEventProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.rabbitmq.custom.transactional-events-exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.custom.invoice-approved-queue}")
    private String invoiceApprovedQueue;

    @Value("${spring.rabbitmq.custom.payment-posted-queue}")
    private String paymentPostedQueue;

    @Value("${spring.rabbitmq.custom.asset-transaction-created-queue}")
    private String assetTransactionCreatedQueue;

    @Value("${spring.rabbitmq.custom.bank-transaction-created-queue}")
    private String bankTransactionCreatedQueue;

    @Value("${spring.rabbitmq.custom.sales-invoice-created-queue}")
    private String salesInvoiceCreatedQueue;

    @Value("${spring.rabbitmq.custom.receipt-created-queue}")
    private String receiptCreatedQueue;

    @Value("${spring.rabbitmq.custom.purchase-order-created-queue}")
    private String purchaseOrderCreatedQueue;

    @Value("${spring.rabbitmq.custom.vendor-created-queue}")
    private String vendorCreatedQueue;

    @Value("${spring.rabbitmq.custom.customer-created-queue}")
    private String customerCreatedQueue;

    @Value("${spring.rabbitmq.custom.fixed-asset-created-queue}")
    private String fixedAssetCreatedQueue;

    public TransactionalEventProducer(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishInvoiceApproved(Object payload) {
        send(invoiceApprovedQueue, payload, "Invoice Approved");
    }

    public void publishPaymentPosted(Object payload) {
        send(paymentPostedQueue, payload, "Payment Posted");
    }

    public void publishAssetTransactionCreated(Object payload) {
        send(assetTransactionCreatedQueue, payload, "Asset Transaction Created");
    }

    public void publishBankTransactionCreated(Object payload) {
        send(bankTransactionCreatedQueue, payload, "Bank Transaction Created");
    }

    public void publishSalesInvoiceCreated(Object payload) {
        send(salesInvoiceCreatedQueue, payload, "Sales Invoice Created");
    }

    public void publishReceiptCreated(Object payload) {
        send(receiptCreatedQueue, payload, "Receipt Created");
    }

    public void publishPurchaseOrderCreated(Object payload) {
        send(purchaseOrderCreatedQueue, payload, "Purchase Order Created");
    }

    public void publishVendorCreated(Object payload) {
        send(vendorCreatedQueue, payload, "Vendor Created");
    }

    public void publishCustomerCreated(Object payload) {
        send(customerCreatedQueue, payload, "Customer Created");
    }

    public void publishFixedAssetCreated(Object payload) {
        send(fixedAssetCreatedQueue, payload, "Fixed Asset Created");
    }

    private void send(String routingKey, Object payload, String eventType) {
        try {
            String eventJson = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(exchange, routingKey, eventJson);
            log.info("✅ {} event sent to routing key: {}", eventType, routingKey);
        } catch (Exception e) {
            log.error("❌ Failed to publish {} event: {}", eventType, e.getMessage());
        }
    }
}
