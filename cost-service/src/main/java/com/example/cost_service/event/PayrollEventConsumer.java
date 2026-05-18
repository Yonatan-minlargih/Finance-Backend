package com.example.cost_service.event;

import com.example.cost_service.dto.eventDto.CostCalculatedEventDto;
import com.example.cost_service.dto.eventDto.PayrollEventDto;
import com.example.cost_service.model.CostRecord;
import com.example.cost_service.repository.CostRecordRepository;
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
public class PayrollEventConsumer {

    private final CostRecordRepository costRecordRepository;
    private final CostEventProducer costEventProducer;

    /**
     * Cross-Service Consumption:
     * Listens to payroll.approved events from the payroll-service.
     * Creates a dedicated queue for cost-service to receive these events.
     */
    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.queue.cost-payroll-approved}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.exchange.payroll}", type = "direct"),
            key = "${rabbitmq.routing-key.payroll.approved}"
        )
    )
    public void handleExternalPayrollApproved(PayrollEventDto event) {
        try {
            log.info("📢 CROSS-SERVICE: Processing Payroll Approved event for database update...");
            
            // Map event to CostRecord entity
            CostRecord costRecord = new CostRecord();
            costRecord.setTenantId(event.getTenantId());
            costRecord.setLaborCost(event.getTotalGross()); // Using Gross Salary as Labor Cost
            costRecord.setMaterialCost(java.math.BigDecimal.ZERO);
            costRecord.setOverheadCost(java.math.BigDecimal.ZERO);
            costRecord.setTotalCost(event.getTotalGross());
            // Use the actual accounting period ID from the payroll run, fall back to payroll run ID if not set
            UUID resolvedPeriodId = event.getPeriodId() != null ? event.getPeriodId() : event.getId();
            costRecord.setPeriodId(resolvedPeriodId);
            costRecord.setCreatedBy("SYSTEM_EVENT_CONSUMER");
            costRecord.setCreatedAt(java.time.LocalDateTime.now());
            
            // Save to database
            CostRecord savedRecord = costRecordRepository.save(costRecord);
            
            log.info("✅ DATABASE UPDATED: Saved Labor Cost record for Payroll: {}", event.getId());
            
            // Bridge the flow: Trigger a cost.calculated event for core-finance-service
            CostCalculatedEventDto calculatedEvent = new CostCalculatedEventDto();
            calculatedEvent.setTenantId(event.getTenantId());
            calculatedEvent.setPeriodId(resolvedPeriodId); // Use the resolved accounting period ID
            calculatedEvent.setLaborCost(event.getTotalGross());
            calculatedEvent.setMaterialCost(java.math.BigDecimal.ZERO);
            calculatedEvent.setOverheadCost(java.math.BigDecimal.ZERO);
            calculatedEvent.setTotalCost(event.getTotalGross());
            calculatedEvent.setCalculatedAt(java.time.LocalDateTime.now());
            calculatedEvent.setCalculatedBy("SYSTEM_PAYROLL_SYNC");
            calculatedEvent.setCalculationType("LABOR_COST_ALLOCATION");
            
            // Note: productId and costCenterId are null as they are not provided by payroll
            
            costEventProducer.sendCostCalculatedEvent(calculatedEvent);
            log.info("📢 EVENT PUBLISHED: Sent cost.calculated event to bridge to Finance service.");
            
        } catch (Exception e) {
            log.error("Error updating database from payroll event: {}", e.getMessage(), e);
        }
    }
}
