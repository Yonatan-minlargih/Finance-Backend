package com.financial.corefinance.event;

import com.financial.corefinance.domain.entity.JournalHeader;
import com.financial.corefinance.dto.eventDto.CostCalculatedEventDto;
import com.financial.corefinance.repository.JournalHeaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CostEventConsumer {

    private final JournalHeaderRepository journalHeaderRepository;

    /**
     * Cross-Service Consumption:
     * Listens to cost.calculated events from the cost-service.
     * Creates a dedicated queue for core-finance-service to receive these events.
     */
    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.costCalculatedQueue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.costEventsExchange}", type = "direct"),
            key = "${rabbitmq.costCalculatedRoutingKey}"
        )
    )
    public void handleExternalCostCalculated(CostCalculatedEventDto event) {
        try {
            log.info("📢 CROSS-SERVICE: Processing Cost Calculated event for financial records...");
            
            // Create a Journal Header to reflect the cost in Finance
            JournalHeader header = JournalHeader.builder()
                    .tenantId(event.getTenantId().toString())
                    .journalNumber("COST-" + java.util.UUID.randomUUID().toString().substring(0, 8))
                    .journalDate(java.time.LocalDate.now())
                    .accountingPeriodId(event.getPeriodId())
                    .journalType(JournalHeader.JournalType.SYSTEM)
                    .description("Auto-Journal: Cost Allocation for Product " + event.getProductId())
                    .status(JournalHeader.JournalStatus.DRAFT)
                    .sourceSystem("COST_SERVICE")
                    .referenceId(event.getProductId())
                    .build();
            
            journalHeaderRepository.save(header);
            
            log.info("✅ DATABASE UPDATED: Created Draft Journal Entry for Cost Calculation of Product: {}", event.getProductId());
            
        } catch (Exception e) {
            log.error("Error updating finance database from cost event: {}", e.getMessage(), e);
        }
    }
}
