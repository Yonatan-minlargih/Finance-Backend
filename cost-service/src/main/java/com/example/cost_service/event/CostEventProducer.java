package com.example.cost_service.event;

import com.example.cost_service.dto.eventDto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CostEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.cost-events-exchange}")
    private String costEventsExchange;

    // MASTER DATA EVENTS
    public void sendCreateProductEvent(ProductEventDto event) {
        send("create.product", event);
    }

    public void sendUpdateProductEvent(ProductEventDto event) {
        send("update.product", event);
    }

    public void sendDeleteProductEvent(ProductEventDto event) {
        send("delete.product", event);
    }

    public void sendCreateCostCenterEvent(CostCenterEventDto event) {
        send("create.costCenter", event);
    }

    public void sendUpdateCostCenterEvent(CostCenterEventDto event) {
        send("update.costCenter", event);
    }

    public void sendDeleteCostCenterEvent(CostCenterEventDto event) {
        send("delete.costCenter", event);
    }

    public void sendCreateProfitCenterEvent(ProfitCenterEventDto event) {
        send("create.profitCenter", event);
    }

    public void sendUpdateProfitCenterEvent(ProfitCenterEventDto event) {
        send("update.profitCenter", event);
    }

    public void sendDeleteProfitCenterEvent(ProfitCenterEventDto event) {
        send("delete.profitCenter", event);
    }

    // COST RECORD EVENTS
    public void sendCreateCostRecordEvent(CostEventDto event) {
        send("create.costRecord", event);
    }

    public void sendUpdateCostRecordEvent(CostEventDto event) {
        send("update.costRecord", event);
    }

    public void sendDeleteCostRecordEvent(CostEventDto event) {
        send("delete.costRecord", event);
    }

    // COST CALCULATION EVENTS
    public void sendCostCalculatedEvent(CostCalculatedEventDto event) {
        send("cost.calculated", event);
    }

    public void sendCogsComputedEvent(CogsComputedEventDto event) {
        send("cogs.computed", event);
    }

    // STANDARD COST RATE EVENTS
    public void sendCreateStandardCostRateEvent(StandardCostRateEventDto event) {
        send("create.standardCostRate", event);
    }

    public void sendUpdateStandardCostRateEvent(StandardCostRateEventDto event) {
        send("update.standardCostRate", event);
    }

    // COGS FORMULA EVENTS
    public void sendCreateCogsFormulaEvent(CogsFormulaEventDto event) {
        send("create.cogsFormula", event);
    }

    public void sendUpdateCogsFormulaEvent(CogsFormulaEventDto event) {
        send("update.cogsFormula", event);
    }

    // PROFITABILITY ANALYSIS EVENTS
    public void sendCreateProfitabilityAnalysisEvent(ProfitabilityAnalysisEventDto event) {
        send("create.profitabilityAnalysis", event);
    }

    public void sendUpdateProfitabilityAnalysisEvent(ProfitabilityAnalysisEventDto event) {
        send("update.profitabilityAnalysis", event);
    }

    // WITHHOLDING TAX EVENTS
    public void sendCreateWithholdingTaxRuleEvent(WithholdingTaxRuleEventDto event) {
        send("create.withholdingTaxRule", event);
    }

    public void sendUpdateWithholdingTaxRuleEvent(WithholdingTaxRuleEventDto event) {
        send("update.withholdingTaxRule", event);
    }

    private void send(String routingKey, Object payload) {
        try {
            log.info("🔍 STEP 2: Sending event to RabbitMQ. Exchange={}, RoutingKey={}, Payload={}", 
                    costEventsExchange, routingKey, payload);
            
            rabbitTemplate.convertAndSend(costEventsExchange, routingKey, payload);
            
            log.info("🔍 STEP 3: Event sent successfully to RabbitMQ. Exchange={}, RoutingKey={}", 
                    costEventsExchange, routingKey);
            log.info("Sent cost event with routing key {}: {}", routingKey, payload);
        } catch (Exception e) {
            log.error("❌ ERROR sending event to RabbitMQ. Exchange={}, RoutingKey={}, Error={}", 
                    costEventsExchange, routingKey, e.getMessage(), e);
            log.error("Failed to send cost event with routing key {}: {}", routingKey, e.getMessage(), e);
        }
    }
}
