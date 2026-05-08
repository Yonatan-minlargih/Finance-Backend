package com.financial.corefinance.event;

import com.financial.corefinance.config.ApplicationProperties;
import com.financial.corefinance.dto.eventDto.FinanceEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinanceEventService {

    private final FinanceEventProducer financeEventProducer;
    private final ApplicationProperties properties;

    // Event Publishing Methods
    public void publishJournalCreatedEvent(UUID journalId, String tenantId, String userId) {
        FinanceEventDto event = FinanceEventDto.journalCreated(journalId, tenantId, userId);
        financeEventProducer.sendCreateJournalEvent(event);
    }

    public void publishJournalUpdatedEvent(UUID journalId, String tenantId, String userId) {
        FinanceEventDto event = FinanceEventDto.journalUpdated(journalId, tenantId, userId);
        financeEventProducer.sendUpdateJournalEvent(event);
    }

    public void publishJournalPostedEvent(UUID journalId, String tenantId, String userId) {
        FinanceEventDto event = FinanceEventDto.journalPosted(journalId, tenantId, userId);
        financeEventProducer.sendUpdateJournalEvent(event); // Using update queue for posted events
    }

    public void publishJournalReversedEvent(UUID journalId, String tenantId, String userId) {
        FinanceEventDto event = FinanceEventDto.journalReversed(journalId, tenantId, userId);
        financeEventProducer.sendUpdateJournalEvent(event); // Using update queue for reversal events
    }

    public void publishAccountCreatedEvent(UUID accountId, String tenantId, String userId) {
        FinanceEventDto event = FinanceEventDto.accountCreated(accountId, tenantId, userId);
        financeEventProducer.sendCreateAccountEvent(event);
    }

    public void publishAccountUpdatedEvent(UUID accountId, String tenantId, String userId) {
        FinanceEventDto event = FinanceEventDto.accountUpdated(accountId, tenantId, userId);
        financeEventProducer.sendUpdateAccountEvent(event);
    }

    public void publishBudgetCreatedEvent(UUID budgetId, String tenantId, String userId) {
        FinanceEventDto event = FinanceEventDto.budgetCreated(budgetId, tenantId, userId);
        financeEventProducer.sendCreateBudgetEvent(event);
    }

    public void publishBudgetUpdatedEvent(UUID budgetId, String tenantId, String userId) {
        FinanceEventDto event = FinanceEventDto.budgetUpdated(budgetId, tenantId, userId);
        financeEventProducer.sendUpdateBudgetEvent(event);
    }

    public void publishFiscalYearCreatedEvent(UUID fiscalYearId, String tenantId, String userId) {
        FinanceEventDto event = FinanceEventDto.fiscalYearCreated(fiscalYearId, tenantId, userId);
        financeEventProducer.sendCreateFiscalYearEvent(event);
    }

    public void publishFiscalYearClosedEvent(UUID fiscalYearId, String tenantId, String userId) {
        FinanceEventDto event = FinanceEventDto.fiscalYearClosed(fiscalYearId, tenantId, userId);
        financeEventProducer.sendCloseFiscalYearEvent(event);
    }

    public void publishReportGeneratedEvent(UUID reportId, String tenantId, String userId) {
        FinanceEventDto event = FinanceEventDto.reportGenerated(reportId, tenantId, userId);
        financeEventProducer.sendGenerateReportEvent(event);
    }

    // Event Consumers
    @RabbitListener(queues = "${rabbitmq.createJournalQueue}")
    public void handleCreateJournalEvent(FinanceEventDto event) {
        log.info("Received create journal event: {} for entity: {}", event.getEventType(), event.getEntityId());
        // Handle create journal event - could trigger notifications, updates, etc.
        handleCreateJournalEventInternal(event);
    }

    @RabbitListener(queues = "${rabbitmq.updateJournalQueue}")
    public void handleUpdateJournalEvent(FinanceEventDto event) {
        log.info("Received update journal event: {} for entity: {}", event.getEventType(), event.getEntityId());
        // Handle update journal event
        handleUpdateJournalEventInternal(event);
    }

    @RabbitListener(queues = "${rabbitmq.createAccountQueue}")
    public void handleCreateAccountEvent(FinanceEventDto event) {
        log.info("Received create account event: {} for entity: {}", event.getEventType(), event.getEntityId());
        // Handle create account event
        handleCreateAccountEventInternal(event);
    }

    @RabbitListener(queues = "${rabbitmq.updateAccountQueue}")
    public void handleUpdateAccountEvent(FinanceEventDto event) {
        log.info("Received update account event: {} for entity: {}", event.getEventType(), event.getEntityId());
        // Handle update account event
        handleUpdateAccountEventInternal(event);
    }

    @RabbitListener(queues = "${rabbitmq.createBudgetQueue}")
    public void handleCreateBudgetEvent(FinanceEventDto event) {
        log.info("Received create budget event: {} for entity: {}", event.getEventType(), event.getEntityId());
        // Handle create budget event
        handleCreateBudgetEventInternal(event);
    }

    @RabbitListener(queues = "${rabbitmq.updateBudgetQueue}")
    public void handleUpdateBudgetEvent(FinanceEventDto event) {
        log.info("Received update budget event: {} for entity: {}", event.getEventType(), event.getEntityId());
        // Handle update budget event
        handleUpdateBudgetEventInternal(event);
    }

    @RabbitListener(queues = "${rabbitmq.createFiscalYearQueue}")
    public void handleCreateFiscalYearEvent(FinanceEventDto event) {
        log.info("Received create fiscal year event: {} for entity: {}", event.getEventType(), event.getEntityId());
        // Handle create fiscal year event
        handleCreateFiscalYearEventInternal(event);
    }

    @RabbitListener(queues = "${rabbitmq.closeFiscalYearQueue}")
    public void handleCloseFiscalYearEvent(FinanceEventDto event) {
        log.info("Received close fiscal year event: {} for entity: {}", event.getEventType(), event.getEntityId());
        // Handle close fiscal year event
        handleCloseFiscalYearEventInternal(event);
    }

    @RabbitListener(queues = "${rabbitmq.generateReportQueue}")
    public void handleGenerateReportEvent(FinanceEventDto event) {
        log.info("Received generate report event: {} for entity: {}", event.getEventType(), event.getEntityId());
        // Handle generate report event
        handleGenerateReportEventInternal(event);
    }

    // Internal Event Handlers
    private void handleCreateJournalEventInternal(FinanceEventDto event) {
        try {
            // Process create journal event
            // This could trigger notifications, cache updates, etc.
            log.debug("Processing create journal event for entity: {}", event.getEntityId());
            
            // Example: Update cache, send notifications, etc.
            
        } catch (Exception e) {
            log.error("Error handling create journal event: {}", e.getMessage(), e);
        }
    }

    private void handleUpdateJournalEventInternal(FinanceEventDto event) {
        try {
            // Process update journal event
            log.debug("Processing update journal event for entity: {}", event.getEntityId());
            
            // Handle different update types
            switch (event.getEventType()) {
                case "JOURNAL_POSTED":
                    handleJournalPosted(event);
                    break;
                case "JOURNAL_REVERSED":
                    handleJournalReversed(event);
                    break;
                case "JOURNAL_UPDATED":
                    handleJournalUpdated(event);
                    break;
                default:
                    log.warn("Unknown journal event type: {}", event.getEventType());
            }
            
        } catch (Exception e) {
            log.error("Error handling update journal event: {}", e.getMessage(), e);
        }
    }

    private void handleCreateAccountEventInternal(FinanceEventDto event) {
        try {
            // Process create account event
            log.debug("Processing create account event for entity: {}", event.getEntityId());
            
            // Could trigger cache updates, notifications, etc.
            
        } catch (Exception e) {
            log.error("Error handling create account event: {}", e.getMessage(), e);
        }
    }

    private void handleUpdateAccountEventInternal(FinanceEventDto event) {
        try {
            // Process update account event
            log.debug("Processing update account event for entity: {}", event.getEntityId());
            
            // Handle account updates
            
        } catch (Exception e) {
            log.error("Error handling update account event: {}", e.getMessage(), e);
        }
    }

    private void handleCreateBudgetEventInternal(FinanceEventDto event) {
        try {
            // Process create budget event
            log.debug("Processing create budget event for entity: {}", event.getEntityId());
            
            // Handle budget creation
            
        } catch (Exception e) {
            log.error("Error handling create budget event: {}", e.getMessage(), e);
        }
    }

    private void handleUpdateBudgetEventInternal(FinanceEventDto event) {
        try {
            // Process update budget event
            log.debug("Processing update budget event for entity: {}", event.getEntityId());
            
            // Handle budget updates
            
        } catch (Exception e) {
            log.error("Error handling update budget event: {}", e.getMessage(), e);
        }
    }

    private void handleCreateFiscalYearEventInternal(FinanceEventDto event) {
        try {
            // Process create fiscal year event
            log.debug("Processing create fiscal year event for entity: {}", event.getEntityId());
            
            // Handle fiscal year creation
            
        } catch (Exception e) {
            log.error("Error handling create fiscal year event: {}", e.getMessage(), e);
        }
    }

    private void handleCloseFiscalYearEventInternal(FinanceEventDto event) {
        try {
            // Process close fiscal year event
            log.debug("Processing close fiscal year event for entity: {}", event.getEntityId());
            
            // Handle fiscal year closing
            
        } catch (Exception e) {
            log.error("Error handling close fiscal year event: {}", e.getMessage(), e);
        }
    }

    private void handleGenerateReportEventInternal(FinanceEventDto event) {
        try {
            // Process generate report event
            log.debug("Processing generate report event for entity: {}", event.getEntityId());
            
            // Handle report generation
            
        } catch (Exception e) {
            log.error("Error handling generate report event: {}", e.getMessage(), e);
        }
    }

    // Specific Event Handlers
    private void handleJournalPosted(FinanceEventDto event) {
        log.info("Journal posted event processed for entity: {}", event.getEntityId());
        // Could trigger notifications, cache updates, etc.
    }

    private void handleJournalReversed(FinanceEventDto event) {
        log.info("Journal reversed event processed for entity: {}", event.getEntityId());
        // Could trigger notifications, cache updates, etc.
    }

    private void handleJournalUpdated(FinanceEventDto event) {
        log.info("Journal updated event processed for entity: {}", event.getEntityId());
        // Could trigger notifications, cache updates, etc.
    }
}
