package com.example.cost_service.event;

import com.example.cost_service.dto.eventDto.*;
import com.example.cost_service.service.CostRecordService;
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

    private final CostRecordService costRecordService;

    // MASTER DATA EVENTS - SEPARATE QUEUES FOR EACH ENTITY TYPE
    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.create-product-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "create.product"
        )
    )
    public void handleCreateProductEvent(ProductEventDto event) {
        try {
            log.info("🎯 CORRECT: Received create product event: {}", event);
            log.info("🎯 CORRECT: Processed create product event for tenant: {}, product id: {}", 
                    event.getTenantId(), event.getId());
            // Add delay to see message in queue before consumption
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            log.error("Error processing create product event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.update-product-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "update.product"
        )
    )
    public void handleUpdateProductEvent(ProductEventDto event) {
        try {
            log.info("🎯 CORRECT: Received update product event: {}", event);
            log.info("🎯 CORRECT: Processed update product event for tenant: {}, product id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing update product event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.delete-product-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "delete.product"
        )
    )
    public void handleDeleteProductEvent(ProductEventDto event) {
        try {
            log.info("🎯 CORRECT: Received delete product event: {}", event);
            log.info("🎯 CORRECT: Processed delete product event for tenant: {}, product id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing delete product event: {}", e.getMessage(), e);
        }
    }

    // COST CENTER EVENTS - DEDICATED QUEUES FOR COST CENTERS ONLY
    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.create-cost-center-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "create.costCenter"
        )
    )
    public void handleCreateCostCenterEvent(CostCenterEventDto event) {
        try {
            log.info("🎯 CORRECT: Received create cost center event: {}", event);
            log.info("🎯 CORRECT: Processed create cost center event for tenant: {}, cost center id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing create cost center event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.update-cost-center-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "update.costCenter"
        )
    )
    public void handleUpdateCostCenterEvent(CostCenterEventDto event) {
        try {
            log.info("🎯 CORRECT: Received update cost center event: {}", event);
            log.info("🎯 CORRECT: Processed update cost center event for tenant: {}, cost center id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing update cost center event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.delete-cost-center-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "delete.costCenter"
        )
    )
    public void handleDeleteCostCenterEvent(CostCenterEventDto event) {
        try {
            log.info("🎯 CORRECT: Received delete cost center event: {}", event);
            log.info("🎯 CORRECT: Processed delete cost center event for tenant: {}, cost center id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing delete cost center event: {}", e.getMessage(), e);
        }
    }

    // PROFIT CENTER EVENTS - DEDICATED QUEUES FOR PROFIT CENTERS ONLY
    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.create-profit-center-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "create.profitCenter"
        )
    )
    public void handleCreateProfitCenterEvent(ProfitCenterEventDto event) {
        try {
            log.info("🎯 CORRECT: Received create profit center event: {}", event);
            log.info("🎯 CORRECT: Processed create profit center event for tenant: {}, profit center id: {}", 
                    event.getTenantId(), event.getId());
            // Add delay to see message in queue before consumption
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            log.error("Error processing create profit center event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.update-profit-center-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "update.profitCenter"
        )
    )
    public void handleUpdateProfitCenterEvent(ProfitCenterEventDto event) {
        try {
            log.info("🎯 CORRECT: Received update profit center event: {}", event);
            log.info("🎯 CORRECT: Processed update profit center event for tenant: {}, profit center id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing update profit center event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.delete-profit-center-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "delete.profitCenter"
        )
    )
    public void handleDeleteProfitCenterEvent(ProfitCenterEventDto event) {
        try {
            log.info("🎯 CORRECT: Received delete profit center event: {}", event);
            log.info("🎯 CORRECT: Processed delete profit center event for tenant: {}, profit center id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing delete profit center event: {}", e.getMessage(), e);
        }
    }

    // COST RECORD EVENTS - DEDICATED QUEUES FOR COST RECORDS ONLY
    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.create-cost-record-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "create.costRecord"
        )
    )
    public void handleCreateCostRecordEvent(CostEventDto event) {
        try {
            log.info("🎯 CORRECT: Received create cost record event: {}", event);
            log.info("🎯 CORRECT: Processed create cost record event for tenant: {}, cost record id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing create cost record event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.update-cost-record-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "update.costRecord"
        )
    )
    public void handleUpdateCostRecordEvent(CostEventDto event) {
        try {
            log.info("🎯 CORRECT: Received update cost record event: {}", event);
            log.info("🎯 CORRECT: Processed update cost record event for tenant: {}, cost record id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing update cost record event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.delete-cost-record-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "delete.costRecord"
        )
    )
    public void handleDeleteCostRecordEvent(CostEventDto event) {
        try {
            log.info("🎯 CORRECT: Received delete cost record event: {}", event);
            log.info("🎯 CORRECT: Processed delete cost record event for tenant: {}, cost record id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing delete cost record event: {}", e.getMessage(), e);
        }
    }

    // COST CALCULATION EVENTS - DEDICATED QUEUES FOR BUSINESS CALCULATIONS ONLY
    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.cost-calculated-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "cost.calculated"
        )
    )
    public void handleCostCalculatedEvent(CostCalculatedEventDto event) {
        try {
            log.info("🎯 CORRECT: Received cost calculated event: {}", event);
            log.info("🎯 CORRECT: Processed cost calculated event for tenant: {}, product id: {}", 
                    event.getTenantId(), event.getProductId());
        } catch (Exception e) {
            log.error("Error processing cost calculated event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.cogs-computed-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "cogs.computed"
        )
    )
    public void handleCogsComputedEvent(CogsComputedEventDto event) {
        try {
            log.info("🎯 CORRECT: Received COGS computed event: {}", event);
            log.info("🎯 CORRECT: Processed COGS computed event for tenant: {}, product id: {}", 
                    event.getTenantId(), event.getProductId());
        } catch (Exception e) {
            log.error("Error processing COGS computed event: {}", e.getMessage(), e);
        }
    }

    // STANDARD COST RATE EVENTS - DEDICATED QUEUES FOR STANDARD COST RATES ONLY
    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.create-standard-cost-rate-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "create.standardCostRate"
        )
    )
    public void handleCreateStandardCostRateEvent(StandardCostRateEventDto event) {
        try {
            log.info("🎯 CORRECT: Received create standard cost rate event: {}", event);
            log.info("🎯 CORRECT: Processed create standard cost rate event for tenant: {}, rate id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing create standard cost rate event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.update-standard-cost-rate-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "update.standardCostRate"
        )
    )
    public void handleUpdateStandardCostRateEvent(StandardCostRateEventDto event) {
        try {
            log.info("🎯 CORRECT: Received update standard cost rate event: {}", event);
            log.info("🎯 CORRECT: Processed update standard cost rate event for tenant: {}, rate id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing update standard cost rate event: {}", e.getMessage(), e);
        }
    }

    // COGS FORMULA EVENTS - DEDICATED QUEUES FOR COGS FORMULAS ONLY
    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.create-cogs-formula-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "create.cogsFormula"
        )
    )
    public void handleCreateCogsFormulaEvent(CogsFormulaEventDto event) {
        try {
            log.info("🎯 CORRECT: Received create COGS formula event: {}", event);
            log.info("🎯 CORRECT: Processed create COGS formula event for tenant: {}, formula id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing create COGS formula event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.update-cogs-formula-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "update.cogsFormula"
        )
    )
    public void handleUpdateCogsFormulaEvent(CogsFormulaEventDto event) {
        try {
            log.info("🎯 CORRECT: Received update COGS formula event: {}", event);
            log.info("🎯 CORRECT: Processed update COGS formula event for tenant: {}, formula id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing update COGS formula event: {}", e.getMessage(), e);
        }
    }

    // PROFITABILITY ANALYSIS EVENTS - DEDICATED QUEUES FOR PROFITABILITY ANALYSIS ONLY
    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.create-profitability-analysis-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "create.profitabilityAnalysis"
        )
    )
    public void handleCreateProfitabilityAnalysisEvent(ProfitabilityAnalysisEventDto event) {
        try {
            log.info("🎯 CORRECT: Received create profitability analysis event: {}", event);
            log.info("🎯 CORRECT: Processed create profitability analysis event for tenant: {}, analysis id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing create profitability analysis event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.update-profitability-analysis-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "update.profitabilityAnalysis"
        )
    )
    public void handleUpdateProfitabilityAnalysisEvent(ProfitabilityAnalysisEventDto event) {
        try {
            log.info("🎯 CORRECT: Received update profitability analysis event: {}", event);
            log.info("🎯 CORRECT: Processed update profitability analysis event for tenant: {}, analysis id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing update profitability analysis event: {}", e.getMessage(), e);
        }
    }

    // WITHHOLDING TAX EVENTS - DEDICATED QUEUES FOR WITHHOLDING TAX RULES ONLY
    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.create-withholding-tax-rule-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "create.withholdingTaxRule"
        )
    )
    public void handleCreateWithholdingTaxRuleEvent(WithholdingTaxRuleEventDto event) {
        try {
            log.info("🎯 CORRECT: Received create withholding tax rule event: {}", event);
            log.info("🎯 CORRECT: Processed create withholding tax rule event for tenant: {}, rule id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing create withholding tax rule event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.update-withholding-tax-rule-queue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.cost-events-exchange}", type = "direct"),
            key = "update.withholdingTaxRule"
        )
    )
    public void handleUpdateWithholdingTaxRuleEvent(WithholdingTaxRuleEventDto event) {
        try {
            log.info("🎯 CORRECT: Received update withholding tax rule event: {}", event);
            log.info("🎯 CORRECT: Processed update withholding tax rule event for tenant: {}, rule id: {}", 
                    event.getTenantId(), event.getId());
        } catch (Exception e) {
            log.error("Error processing update withholding tax rule event: {}", e.getMessage(), e);
        }
    }
}
