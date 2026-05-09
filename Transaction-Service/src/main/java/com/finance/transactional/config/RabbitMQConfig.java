package com.finance.transactional.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQConfig {

    private final ConnectionFactory connectionFactory;

    public RabbitMQConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Value("${spring.rabbitmq.custom.transactional-events-exchange}")
    private String exchangeName;

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

    @PostConstruct
    public void init() {
        try {
            log.info("🔍 Testing RabbitMQ connection on startup...");
            connectionFactory.createConnection().close();
            log.info("✅ RabbitMQ connection successful!");
        } catch (Exception e) {
            log.error("❌ RabbitMQ connection failed: {}", e.getMessage());
        }
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        log.info("🚀 RabbitAdmin initialized for automatic declaration");
        return admin;
    }

    @Bean
    public DirectExchange transactionalExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue invoiceApprovedQueue() {
        return QueueBuilder.durable(invoiceApprovedQueue).build();
    }

    @Bean
    public Binding invoiceApprovedBinding(Queue invoiceApprovedQueue, DirectExchange transactionalExchange) {
        return BindingBuilder.bind(invoiceApprovedQueue).to(transactionalExchange).with(this.invoiceApprovedQueue);
    }

    @Bean
    public Queue paymentPostedQueue() {
        return QueueBuilder.durable(paymentPostedQueue).build();
    }

    @Bean
    public Binding paymentPostedBinding(Queue paymentPostedQueue, DirectExchange transactionalExchange) {
        return BindingBuilder.bind(paymentPostedQueue).to(transactionalExchange).with(this.paymentPostedQueue);
    }

    @Bean
    public Queue assetTransactionCreatedQueue() {
        return QueueBuilder.durable(assetTransactionCreatedQueue).build();
    }

    @Bean
    public Binding assetTransactionCreatedBinding(Queue assetTransactionCreatedQueue, DirectExchange transactionalExchange) {
        return BindingBuilder.bind(assetTransactionCreatedQueue).to(transactionalExchange).with(this.assetTransactionCreatedQueue);
    }

    @Bean
    public Queue bankTransactionCreatedQueue() {
        return QueueBuilder.durable(bankTransactionCreatedQueue).build();
    }

    @Bean
    public Binding bankTransactionCreatedBinding(Queue bankTransactionCreatedQueue, DirectExchange transactionalExchange) {
        return BindingBuilder.bind(bankTransactionCreatedQueue).to(transactionalExchange).with(this.bankTransactionCreatedQueue);
    }

    @Bean
    public Queue salesInvoiceCreatedQueue() {
        return QueueBuilder.durable(salesInvoiceCreatedQueue).build();
    }

    @Bean
    public Binding salesInvoiceCreatedBinding(Queue salesInvoiceCreatedQueue, DirectExchange transactionalExchange) {
        return BindingBuilder.bind(salesInvoiceCreatedQueue).to(transactionalExchange).with(this.salesInvoiceCreatedQueue);
    }

    @Bean
    public Queue receiptCreatedQueue() {
        return QueueBuilder.durable(receiptCreatedQueue).build();
    }

    @Bean
    public Binding receiptCreatedBinding(Queue receiptCreatedQueue, DirectExchange transactionalExchange) {
        return BindingBuilder.bind(receiptCreatedQueue).to(transactionalExchange).with(this.receiptCreatedQueue);
    }

    @Bean
    public Queue purchaseOrderCreatedQueue() {
        return QueueBuilder.durable(purchaseOrderCreatedQueue).build();
    }

    @Bean
    public Binding purchaseOrderCreatedBinding(Queue purchaseOrderCreatedQueue, DirectExchange transactionalExchange) {
        return BindingBuilder.bind(purchaseOrderCreatedQueue).to(transactionalExchange).with(this.purchaseOrderCreatedQueue);
    }

    @Bean
    public Queue vendorCreatedQueue() {
        return QueueBuilder.durable(vendorCreatedQueue).build();
    }

    @Bean
    public Binding vendorCreatedBinding(Queue vendorCreatedQueue, DirectExchange transactionalExchange) {
        return BindingBuilder.bind(vendorCreatedQueue).to(transactionalExchange).with(this.vendorCreatedQueue);
    }

    @Bean
    public Queue customerCreatedQueue() {
        return QueueBuilder.durable(customerCreatedQueue).build();
    }

    @Bean
    public Binding customerCreatedBinding(Queue customerCreatedQueue, DirectExchange transactionalExchange) {
        return BindingBuilder.bind(customerCreatedQueue).to(transactionalExchange).with(this.customerCreatedQueue);
    }

    @Bean
    public Queue fixedAssetCreatedQueue() {
        return QueueBuilder.durable(fixedAssetCreatedQueue).build();
    }

    @Bean
    public Binding fixedAssetCreatedBinding(Queue fixedAssetCreatedQueue, DirectExchange transactionalExchange) {
        return BindingBuilder.bind(fixedAssetCreatedQueue).to(transactionalExchange).with(this.fixedAssetCreatedQueue);
    }

    @Bean
    public Jackson2JsonMessageConverter jacksonConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, ObjectMapper mapper) {
        final RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonConverter(mapper));

        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("✅ Message confirmed by exchange: {}", correlationData);
            } else {
                log.error("❌ Message not confirmed by exchange: {}, cause: {}", correlationData, cause);
            }
        });

        template.setReturnsCallback(returned -> {
            log.error("❌ Message returned: replyCode={}, replyText={}, exchange={}, routingKey={}",
                    returned.getReplyCode(), returned.getReplyText(),
                    returned.getExchange(), returned.getRoutingKey());
        });

        template.setMandatory(true);

        return template;
    }
}
