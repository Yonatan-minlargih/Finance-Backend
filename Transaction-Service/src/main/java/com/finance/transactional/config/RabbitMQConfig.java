package com.finance.transactional.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

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

    @Value("${spring.rabbitmq.custom.sales-invoice-approved-queue}")
    private String salesInvoiceApprovedQueue;

    @Value("${spring.rabbitmq.custom.receipt-created-queue}")
    private String receiptCreatedQueue;

    @Value("${spring.rabbitmq.custom.receipt-posted-queue}")
    private String receiptPostedQueue;

    @Value("${spring.rabbitmq.custom.ar-write-off-queue}")
    private String arWriteOffQueue;

    @Value("${spring.rabbitmq.custom.ar-interest-queue}")
    private String arInterestQueue;

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
    public Queue salesInvoiceApprovedQueueBean() {
        return QueueBuilder.durable(salesInvoiceApprovedQueue).build();
    }

    @Bean
    public Binding salesInvoiceApprovedBinding(Queue salesInvoiceApprovedQueueBean, DirectExchange transactionalExchange) {
        return BindingBuilder.bind(salesInvoiceApprovedQueueBean).to(transactionalExchange).with(this.salesInvoiceApprovedQueue);
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
    public Queue receiptPostedQueueBean() {
        return QueueBuilder.durable(receiptPostedQueue).build();
    }

    @Bean
    public Binding receiptPostedBinding(Queue receiptPostedQueueBean, DirectExchange transactionalExchange) {
        return BindingBuilder.bind(receiptPostedQueueBean).to(transactionalExchange).with(this.receiptPostedQueue);
    }

    @Bean
    public Queue arWriteOffQueueBean() {
        return QueueBuilder.durable(arWriteOffQueue).build();
    }

    @Bean
    public Binding arWriteOffBinding(Queue arWriteOffQueueBean, DirectExchange transactionalExchange) {
        return BindingBuilder.bind(arWriteOffQueueBean).to(transactionalExchange).with(this.arWriteOffQueue);
    }

    @Bean
    public Queue arInterestQueueBean() {
        return QueueBuilder.durable(arInterestQueue).build();
    }

    @Bean
    public Binding arInterestBinding(Queue arInterestQueueBean, DirectExchange transactionalExchange) {
        return BindingBuilder.bind(arInterestQueueBean).to(transactionalExchange).with(this.arInterestQueue);
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
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(mapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put(
                "com.financial.corefinance.dto.event.ApInvoiceGlPostResult",
                com.finance.transactional.dto.event.ApInvoiceGlPostResult.class);
        idClassMapping.put(
                "com.financial.corefinance.dto.event.ApInvoiceApprovedEvent",
                com.finance.transactional.dto.event.ApInvoiceApprovedEvent.class);
        idClassMapping.put(
                "com.financial.corefinance.dto.event.ArSalesInvoiceApprovedEvent",
                com.finance.transactional.dto.event.ArSalesInvoiceApprovedEvent.class);
        idClassMapping.put(
                "com.financial.corefinance.dto.event.ArSalesInvoiceGlPostResult",
                com.finance.transactional.dto.event.ArSalesInvoiceGlPostResult.class);
        idClassMapping.put(
                "com.financial.corefinance.dto.event.ArReceiptPostedEvent",
                com.finance.transactional.dto.event.ArReceiptPostedEvent.class);
        idClassMapping.put(
                "com.financial.corefinance.dto.event.ArReceiptGlPostResult",
                com.finance.transactional.dto.event.ArReceiptGlPostResult.class);
        idClassMapping.put(
                "com.financial.corefinance.dto.event.ApPaymentPostedEvent",
                com.finance.transactional.dto.event.ApPaymentPostedEvent.class);
        idClassMapping.put(
                "com.financial.corefinance.dto.event.ApPaymentGlPostResult",
                com.finance.transactional.dto.event.ApPaymentGlPostResult.class);
        typeMapper.setIdClassMapping(idClassMapping);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
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
