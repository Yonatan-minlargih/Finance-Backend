package com.finance.transactional.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq")
public record ApplicationProperties(
        String transactionalEventsExchange,
        String invoiceApprovedQueue,
        String paymentPostedQueue,
        String publishTransactionalEventsJobCron) {}

