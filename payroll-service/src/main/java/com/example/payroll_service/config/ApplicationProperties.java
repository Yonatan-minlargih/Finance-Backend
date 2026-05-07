package com.example.payroll_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq")
public record ApplicationProperties(
        String costEventsExchange,
        String createCostQueue,
        String updateCostQueue,
        String deleteCostQueue) {
}
