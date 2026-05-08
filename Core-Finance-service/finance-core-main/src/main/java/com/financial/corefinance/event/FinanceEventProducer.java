package com.financial.corefinance.event;

import com.financial.corefinance.config.ApplicationProperties;
import com.financial.corefinance.dto.eventDto.FinanceEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinanceEventProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ApplicationProperties properties;

    // Journal Events
    public void sendCreateJournalEvent(FinanceEventDto event) {
        log.info("Sending create journal event: {}", event.getEventType());
        this.send(properties.createJournalQueue(), event);
    }

    public void sendUpdateJournalEvent(FinanceEventDto event) {
        log.info("Sending update journal event: {}", event.getEventType());
        this.send(properties.updateJournalQueue(), event);
    }

    public void sendDeleteJournalEvent(FinanceEventDto event) {
        log.info("Sending delete journal event: {}", event.getEventType());
        this.send(properties.deleteJournalQueue(), event);
    }

    // Account Events
    public void sendCreateAccountEvent(FinanceEventDto event) {
        log.info("Sending create account event: {}", event.getEventType());
        this.send(properties.createAccountQueue(), event);
    }

    public void sendUpdateAccountEvent(FinanceEventDto event) {
        log.info("Sending update account event: {}", event.getEventType());
        this.send(properties.updateAccountQueue(), event);
    }

    public void sendDeleteAccountEvent(FinanceEventDto event) {
        log.info("Sending delete account event: {}", event.getEventType());
        this.send(properties.deleteAccountQueue(), event);
    }

    // Budget Events
    public void sendCreateBudgetEvent(FinanceEventDto event) {
        log.info("Sending create budget event: {}", event.getEventType());
        this.send(properties.createBudgetQueue(), event);
    }

    public void sendUpdateBudgetEvent(FinanceEventDto event) {
        log.info("Sending update budget event: {}", event.getEventType());
        this.send(properties.updateBudgetQueue(), event);
    }

    // Fiscal Year Events
    public void sendCreateFiscalYearEvent(FinanceEventDto event) {
        log.info("Sending create fiscal year event: {}", event.getEventType());
        this.send(properties.createFiscalYearQueue(), event);
    }

    public void sendCloseFiscalYearEvent(FinanceEventDto event) {
        log.info("Sending close fiscal year event: {}", event.getEventType());
        this.send(properties.closeFiscalYearQueue(), event);
    }

    // Report Generation Events
    public void sendGenerateReportEvent(FinanceEventDto event) {
        log.info("Sending generate report event: {}", event.getEventType());
        this.send(properties.generateReportQueue(), event);
    }

    private void send(String routingKey, Object payload) {
        try {
            rabbitTemplate.convertAndSend(properties.financeEventsExchange(), routingKey, payload);
        } catch (Exception e) {
            log.error("Failed to send event to queue: {}", routingKey, e);
        }
    }
}
