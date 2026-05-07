package com.example.payroll_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Simple RabbitMQ Configuration for Payroll Service
 * 
 * This configuration follows the direct publishing approach (similar to cost-service)
 * with minimal complexity and no event store pattern.
 */
@Configuration
@Slf4j
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.payroll}")
    private String payrollExchange;

    @Value("${rabbitmq.queue.payroll-created}")
    private String payrollCreatedQueue;

    @Value("${rabbitmq.queue.payroll-processed}")
    private String payrollProcessedQueue;

    @Value("${rabbitmq.queue.payroll-approved}")
    private String payrollApprovedQueue;

    @Value("${rabbitmq.queue.payroll-cancelled}")
    private String payrollCancelledQueue;

    @Value("${rabbitmq.queue.loan-payment}")
    private String loanPaymentQueue;

    @Value("${rabbitmq.queue.salary-component}")
    private String salaryComponentQueue;

    /**
     * Payroll Events Exchange
     */
    @Bean
    public DirectExchange payrollExchange() {
        return new DirectExchange(payrollExchange, true, false);
    }

    /**
     * Payroll Created Queue
     */
    @Bean
    public Queue payrollCreatedQueue() {
        return QueueBuilder.durable(payrollCreatedQueue).build();
    }

    /**
     * Payroll Processed Queue
     */
    @Bean
    public Queue payrollProcessedQueue() {
        return QueueBuilder.durable(payrollProcessedQueue).build();
    }

    /**
     * Payroll Approved Queue
     */
    @Bean
    public Queue payrollApprovedQueue() {
        return QueueBuilder.durable(payrollApprovedQueue).build();
    }

    /**
     * Payroll Cancelled Queue
     */
    @Bean
    public Queue payrollCancelledQueue() {
        return QueueBuilder.durable(payrollCancelledQueue).build();
    }

    /**
     * Loan Payment Queue
     */
    @Bean
    public Queue loanPaymentQueue() {
        return QueueBuilder.durable(loanPaymentQueue).build();
    }

    /**
     * Salary Component Queue
     */
    @Bean
    public Queue salaryComponentQueue() {
        return QueueBuilder.durable(salaryComponentQueue).build();
    }

    /**
     * Binding for Payroll Created Queue
     */
    @Bean
    public Binding payrollCreatedBinding() {
        return BindingBuilder
                .bind(payrollCreatedQueue())
                .to(payrollExchange())
                .with("payroll.created");
    }

    /**
     * Binding for Payroll Processed Queue
     */
    @Bean
    public Binding payrollProcessedBinding() {
        return BindingBuilder
                .bind(payrollProcessedQueue())
                .to(payrollExchange())
                .with("payroll.processed");
    }

    /**
     * Binding for Payroll Approved Queue
     */
    @Bean
    public Binding payrollApprovedBinding() {
        return BindingBuilder
                .bind(payrollApprovedQueue())
                .to(payrollExchange())
                .with("payroll.approved");
    }

    /**
     * Binding for Payroll Cancelled Queue
     */
    @Bean
    public Binding payrollCancelledBinding() {
        return BindingBuilder
                .bind(payrollCancelledQueue())
                .to(payrollExchange())
                .with("payroll.cancelled");
    }

    /**
     * Binding for Loan Payment Queue
     */
    @Bean
    public Binding loanPaymentBinding() {
        return BindingBuilder
                .bind(loanPaymentQueue())
                .to(payrollExchange())
                .with("loan.payment");
    }

    /**
     * Binding for Salary Component Queue
     */
    @Bean
    public Binding salaryComponentBinding() {
        return BindingBuilder
                .bind(salaryComponentQueue())
                .to(payrollExchange())
                .with("salary.component");
    }

    /**
     * JSON Message Converter
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * RabbitTemplate with publisher confirms
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                       Jackson2JsonMessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);

        // Enable publisher confirms for reliable message delivery
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("✅ Message confirmed by exchange: {}", correlationData);
            } else {
                log.error("❌ Message not confirmed by exchange: {}, cause: {}", correlationData, cause);
            }
        });

        // Enable publisher returns for unroutable messages
        template.setReturnsCallback(returned -> {
            log.error("❌ Message returned: replyCode={}, replyText={}, exchange={}, routingKey={}",
                    returned.getReplyCode(), returned.getReplyText(),
                    returned.getExchange(), returned.getRoutingKey());
        });

        // Set mandatory flag to ensure messages are returned if they can't be routed
        template.setMandatory(true);

        return template;
    }
}