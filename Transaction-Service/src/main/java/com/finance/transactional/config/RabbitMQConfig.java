package com.finance.transactional.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final ApplicationProperties properties;

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(properties.transactionalEventsExchange());
    }

    @Bean
    public Queue invoiceApprovedQueue() {
        return QueueBuilder.durable(properties.invoiceApprovedQueue()).build();
    }

    @Bean
    public Binding invoiceApprovedBinding() {
        return BindingBuilder.bind(invoiceApprovedQueue()).to(exchange()).with(properties.invoiceApprovedQueue());
    }

    @Bean
    public Queue paymentPostedQueue() {
        return QueueBuilder.durable(properties.paymentPostedQueue()).build();
    }

    @Bean
    public Binding paymentPostedBinding() {
        return BindingBuilder.bind(paymentPostedQueue()).to(exchange()).with(properties.paymentPostedQueue());
    }

    @Bean
    public Jackson2JsonMessageConverter jacksonConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory factory, ObjectMapper mapper) {
        final var rabbitTemplate = new RabbitTemplate(factory);
        rabbitTemplate.setMessageConverter(jacksonConverter(mapper));
        return rabbitTemplate;
    }
}

