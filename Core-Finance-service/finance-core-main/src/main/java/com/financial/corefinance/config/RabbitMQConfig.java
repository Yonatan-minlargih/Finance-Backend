package com.financial.corefinance.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConfig {

    private final ApplicationProperties properties;
    private final ConnectionFactory connectionFactory;

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
    public DirectExchange exchange() {
        return new DirectExchange(properties.financeEventsExchange());
    }

    // Journal Event Queues
    @Bean
    public Queue createJournalQueue() {
        return QueueBuilder.durable(properties.createJournalQueue()).build();
    }

    @Bean
    public Binding createJournalBinding() {
        return BindingBuilder.bind(createJournalQueue()).to(exchange()).with(properties.createJournalQueue());
    }

    @Bean
    public Queue updateJournalQueue() {
        return QueueBuilder.durable(properties.updateJournalQueue()).build();
    }

    @Bean
    public Binding updateJournalBinding() {
        return BindingBuilder.bind(updateJournalQueue()).to(exchange()).with(properties.updateJournalQueue());
    }

    @Bean
    public Queue deleteJournalQueue() {
        return QueueBuilder.durable(properties.deleteJournalQueue()).build();
    }

    @Bean
    public Binding deleteJournalBinding() {
        return BindingBuilder.bind(deleteJournalQueue()).to(exchange()).with(properties.deleteJournalQueue());
    }

    // Account Event Queues
    @Bean
    public Queue createAccountQueue() {
        return QueueBuilder.durable(properties.createAccountQueue()).build();
    }

    @Bean
    public Binding createAccountBinding() {
        return BindingBuilder.bind(createAccountQueue()).to(exchange()).with(properties.createAccountQueue());
    }

    @Bean
    public Queue updateAccountQueue() {
        return QueueBuilder.durable(properties.updateAccountQueue()).build();
    }

    @Bean
    public Binding updateAccountBinding() {
        return BindingBuilder.bind(updateAccountQueue()).to(exchange()).with(properties.updateAccountQueue());
    }

    @Bean
    public Queue deleteAccountQueue() {
        return QueueBuilder.durable(properties.deleteAccountQueue()).build();
    }

    @Bean
    public Binding deleteAccountBinding() {
        return BindingBuilder.bind(deleteAccountQueue()).to(exchange()).with(properties.deleteAccountQueue());
    }

    // Budget Event Queues
    @Bean
    public Queue createBudgetQueue() {
        return QueueBuilder.durable(properties.createBudgetQueue()).build();
    }

    @Bean
    public Binding createBudgetBinding() {
        return BindingBuilder.bind(createBudgetQueue()).to(exchange()).with(properties.createBudgetQueue());
    }

    @Bean
    public Queue updateBudgetQueue() {
        return QueueBuilder.durable(properties.updateBudgetQueue()).build();
    }

    @Bean
    public Binding updateBudgetBinding() {
        return BindingBuilder.bind(updateBudgetQueue()).to(exchange()).with(properties.updateBudgetQueue());
    }

    // Fiscal Year Event Queues
    @Bean
    public Queue createFiscalYearQueue() {
        return QueueBuilder.durable(properties.createFiscalYearQueue()).build();
    }

    @Bean
    public Binding createFiscalYearBinding() {
        return BindingBuilder.bind(createFiscalYearQueue()).to(exchange()).with(properties.createFiscalYearQueue());
    }

    @Bean
    public Queue closeFiscalYearQueue() {
        return QueueBuilder.durable(properties.closeFiscalYearQueue()).build();
    }

    @Bean
    public Binding closeFiscalYearBinding() {
        return BindingBuilder.bind(closeFiscalYearQueue()).to(exchange()).with(properties.closeFiscalYearQueue());
    }

    // Report Generation Queue
    @Bean
    public Queue generateReportQueue() {
        return QueueBuilder.durable(properties.generateReportQueue()).build();
    }

    @Bean
    public Binding generateReportBinding() {
        return BindingBuilder.bind(generateReportQueue()).to(exchange()).with(properties.generateReportQueue());
    }

    @Bean
    public Jackson2JsonMessageConverter jacksonConverter(ObjectMapper mapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(mapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory factory, ObjectMapper mapper) {
        final RabbitTemplate template = new RabbitTemplate(factory);
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
