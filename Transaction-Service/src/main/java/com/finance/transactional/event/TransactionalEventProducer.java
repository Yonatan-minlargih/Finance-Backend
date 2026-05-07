package com.finance.transactional.event;

import com.finance.transactional.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionalEventProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ApplicationProperties properties;

    public void publishInvoiceApproved(Object payload) {
        send(properties.invoiceApprovedQueue(), payload);
    }

    public void publishPaymentPosted(Object payload) {
        send(properties.paymentPostedQueue(), payload);
    }

    private void send(String routingKey, Object payload) {
        rabbitTemplate.convertAndSend(properties.transactionalEventsExchange(), routingKey, payload);
    }
}

