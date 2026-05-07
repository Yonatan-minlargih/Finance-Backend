package com.example.payroll_service.event;

import com.example.payroll_service.dto.eventDto.LoanEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanEventProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rabbitmq.exchange.loan}")
    private String loanExchange;

    @Value("${rabbitmq.routing-key.loan.created}")
    private String loanCreatedRoutingKey;

    @Value("${rabbitmq.routing-key.loan.updated}")
    private String loanUpdatedRoutingKey;

    @Value("${rabbitmq.routing-key.loan.payment}")
    private String loanPaymentRoutingKey;

    public void sendLoanCreatedEvent(LoanEventDto event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(loanExchange, loanCreatedRoutingKey, eventJson);
            log.info("Loan created event sent for loan id: {}", event.getId());
        } catch (Exception e) {
            log.error("Failed to send loan created event for loan id: {}", event.getId(), e);
        }
    }

    public void sendLoanUpdatedEvent(LoanEventDto event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(loanExchange, loanUpdatedRoutingKey, eventJson);
            log.info("Loan updated event sent for loan id: {}", event.getId());
        } catch (Exception e) {
            log.error("Failed to send loan updated event for loan id: {}", event.getId(), e);
        }
    }

    public void sendLoanPaymentEvent(LoanEventDto event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(loanExchange, loanPaymentRoutingKey, eventJson);
            log.info("Loan payment event sent for loan id: {}", event.getId());
        } catch (Exception e) {
            log.error("Failed to send loan payment event for loan id: {}", event.getId(), e);
        }
    }
}
