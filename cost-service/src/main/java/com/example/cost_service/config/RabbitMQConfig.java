package com.example.cost_service.config;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Configuration
@Slf4j
public class RabbitMQConfig {

    @Autowired
    private Environment env;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Value("${rabbitmq.cost-events-exchange}")
    private String costEventsExchange;

    @Bean
    public DirectExchange costEventsExchange() {
        return new DirectExchange(costEventsExchange);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    // === SEPARATE QUEUES FOR EACH ENTITY TYPE TO ELIMINATE CONSUMER CONFLICTS ===

    // COST RECORD QUEUES
    @Bean
    public Queue createCostRecordQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.create-cost-record-queue")).build();
    }

    @Bean
    public Queue updateCostRecordQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.update-cost-record-queue")).build();
    }

    @Bean
    public Queue deleteCostRecordQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.delete-cost-record-queue")).build();
    }

    // PRODUCT QUEUES
    @Bean
    public Queue createProductQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.create-product-queue")).build();
    }

    @Bean
    public Queue updateProductQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.update-product-queue")).build();
    }

    @Bean
    public Queue deleteProductQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.delete-product-queue")).build();
    }

    // COST CENTER QUEUES
    @Bean
    public Queue createCostCenterQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.create-cost-center-queue")).build();
    }

    @Bean
    public Queue updateCostCenterQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.update-cost-center-queue")).build();
    }

    @Bean
    public Queue deleteCostCenterQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.delete-cost-center-queue")).build();
    }

    // PROFIT CENTER QUEUES
    @Bean
    public Queue createProfitCenterQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.create-profit-center-queue")).build();
    }

    @Bean
    public Queue updateProfitCenterQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.update-profit-center-queue")).build();
    }

    @Bean
    public Queue deleteProfitCenterQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.delete-profit-center-queue")).build();
    }

    // STANDARD COST RATE QUEUES
    @Bean
    public Queue createStandardCostRateQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.create-standard-cost-rate-queue")).build();
    }

    @Bean
    public Queue updateStandardCostRateQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.update-standard-cost-rate-queue")).build();
    }

    // COGS FORMULA QUEUES
    @Bean
    public Queue createCogsFormulaQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.create-cogs-formula-queue")).build();
    }

    @Bean
    public Queue updateCogsFormulaQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.update-cogs-formula-queue")).build();
    }

    // PROFITABILITY ANALYSIS QUEUES
    @Bean
    public Queue createProfitabilityAnalysisQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.create-profitability-analysis-queue")).build();
    }

    @Bean
    public Queue updateProfitabilityAnalysisQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.update-profitability-analysis-queue")).build();
    }

    // WITHHOLDING TAX RULE QUEUES
    @Bean
    public Queue createWithholdingTaxRuleQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.create-withholding-tax-rule-queue")).build();
    }

    @Bean
    public Queue updateWithholdingTaxRuleQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.update-withholding-tax-rule-queue")).build();
    }

    // BUSINESS CALCULATION QUEUES
    @Bean
    public Queue costCalculatedQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.cost-calculated-queue")).build();
    }

    @Bean
    public Queue cogsComputedQueue() {
        return QueueBuilder.durable(env.getProperty("rabbitmq.cogs-computed-queue")).build();
    }

    // === SEPARATE BINDINGS FOR EACH ENTITY TYPE - NO MORE QUEUE SHARING ===
    
    // COST RECORD EVENT BINDINGS - DEDICATED QUEUES
    @Bean
    public Binding createCostRecordBinding() {
        return BindingBuilder.bind(createCostRecordQueue())
                .to(costEventsExchange())
                .with("create.costRecord");
    }

    @Bean
    public Binding updateCostRecordBinding() {
        return BindingBuilder.bind(updateCostRecordQueue())
                .to(costEventsExchange())
                .with("update.costRecord");
    }

    @Bean
    public Binding deleteCostRecordBinding() {
        return BindingBuilder.bind(deleteCostRecordQueue())
                .to(costEventsExchange())
                .with("delete.costRecord");
    }

    // PRODUCT EVENT BINDINGS - DEDICATED QUEUES
    @Bean
    public Binding createProductBinding() {
        return BindingBuilder.bind(createProductQueue())
                .to(costEventsExchange())
                .with("create.product");
    }

    @Bean
    public Binding updateProductBinding() {
        return BindingBuilder.bind(updateProductQueue())
                .to(costEventsExchange())
                .with("update.product");
    }

    @Bean
    public Binding deleteProductBinding() {
        return BindingBuilder.bind(deleteProductQueue())
                .to(costEventsExchange())
                .with("delete.product");
    }

    // COST CENTER EVENT BINDINGS - DEDICATED QUEUES
    @Bean
    public Binding createCostCenterBinding() {
        return BindingBuilder.bind(createCostCenterQueue())
                .to(costEventsExchange())
                .with("create.costCenter");
    }

    @Bean
    public Binding updateCostCenterBinding() {
        return BindingBuilder.bind(updateCostCenterQueue())
                .to(costEventsExchange())
                .with("update.costCenter");
    }

    @Bean
    public Binding deleteCostCenterBinding() {
        return BindingBuilder.bind(deleteCostCenterQueue())
                .to(costEventsExchange())
                .with("delete.costCenter");
    }

    // PROFIT CENTER EVENT BINDINGS - DEDICATED QUEUES
    @Bean
    public Binding createProfitCenterBinding() {
        return BindingBuilder.bind(createProfitCenterQueue())
                .to(costEventsExchange())
                .with("create.profitCenter");
    }

    @Bean
    public Binding updateProfitCenterBinding() {
        return BindingBuilder.bind(updateProfitCenterQueue())
                .to(costEventsExchange())
                .with("update.profitCenter");
    }

    @Bean
    public Binding deleteProfitCenterBinding() {
        return BindingBuilder.bind(deleteProfitCenterQueue())
                .to(costEventsExchange())
                .with("delete.profitCenter");
    }

    // STANDARD COST RATE EVENT BINDINGS - DEDICATED QUEUES
    @Bean
    public Binding createStandardCostRateBinding() {
        return BindingBuilder.bind(createStandardCostRateQueue())
                .to(costEventsExchange())
                .with("create.standardCostRate");
    }

    @Bean
    public Binding updateStandardCostRateBinding() {
        return BindingBuilder.bind(updateStandardCostRateQueue())
                .to(costEventsExchange())
                .with("update.standardCostRate");
    }

    // COGS FORMULA EVENT BINDINGS - DEDICATED QUEUES
    @Bean
    public Binding createCogsFormulaBinding() {
        return BindingBuilder.bind(createCogsFormulaQueue())
                .to(costEventsExchange())
                .with("create.cogsFormula");
    }

    @Bean
    public Binding updateCogsFormulaBinding() {
        return BindingBuilder.bind(updateCogsFormulaQueue())
                .to(costEventsExchange())
                .with("update.cogsFormula");
    }

    // PROFITABILITY ANALYSIS EVENT BINDINGS - DEDICATED QUEUES
    @Bean
    public Binding createProfitabilityAnalysisBinding() {
        return BindingBuilder.bind(createProfitabilityAnalysisQueue())
                .to(costEventsExchange())
                .with("create.profitabilityAnalysis");
    }

    @Bean
    public Binding updateProfitabilityAnalysisBinding() {
        return BindingBuilder.bind(updateProfitabilityAnalysisQueue())
                .to(costEventsExchange())
                .with("update.profitabilityAnalysis");
    }

    // WITHHOLDING TAX RULE EVENT BINDINGS - DEDICATED QUEUES
    @Bean
    public Binding createWithholdingTaxRuleBinding() {
        return BindingBuilder.bind(createWithholdingTaxRuleQueue())
                .to(costEventsExchange())
                .with("create.withholdingTaxRule");
    }

    @Bean
    public Binding updateWithholdingTaxRuleBinding() {
        return BindingBuilder.bind(updateWithholdingTaxRuleQueue())
                .to(costEventsExchange())
                .with("update.withholdingTaxRule");
    }

    // BUSINESS CALCULATION EVENT BINDINGS - DEDICATED QUEUES
    @Bean
    public Binding costCalculatedBinding() {
        return BindingBuilder.bind(costCalculatedQueue())
                .to(costEventsExchange())
                .with("cost.calculated");
    }

    @Bean
    public Binding cogsComputedBinding() {
        return BindingBuilder.bind(cogsComputedQueue())
                .to(costEventsExchange())
                .with("cogs.computed");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        
        // Enable publisher confirms
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("✅ MESSAGE ACKED BY EXCHANGE - CorrelationData={}", correlationData);
            } else {
                log.error("❌ MESSAGE NOT ACKED - CorrelationData={}, Cause={}", correlationData, cause);
            }
        });
        
        // Enable publisher returns for undeliverable messages
        template.setReturnsCallback(returned -> {
            log.error("❌ Message returned! Exchange={}, RoutingKey={}, ReplyText={}, Message={}",
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getReplyText(),
                    returned.getMessage());
        });
        
        // Set mandatory flag to trigger returns callback
        template.setMandatory(true);
        
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

    @PostConstruct
    public void logConfig() {
        // Clean up old generic queues before logging configuration
        cleanupOldGenericQueues();

        log.info("🔍 === RABBITMQ SEPARATE QUEUE CONFIGURATION ===");
        log.info("Exchange Type: DIRECT");
        log.info("Exchange Name: {}", costEventsExchange);
        log.info("");
        log.info("📋 SEPARATE QUEUES FOR EACH ENTITY TYPE:");
        log.info("");
        log.info("COST RECORD QUEUES:");
        log.info("  - create.costRecord → {}", env.getProperty("rabbitmq.create-cost-record-queue"));
        log.info("  - update.costRecord → {}", env.getProperty("rabbitmq.update-cost-record-queue"));
        log.info("  - delete.costRecord → {}", env.getProperty("rabbitmq.delete-cost-record-queue"));
        log.info("");
        log.info("PRODUCT QUEUES:");
        log.info("  - create.product → {}", env.getProperty("rabbitmq.create-product-queue"));
        log.info("  - update.product → {}", env.getProperty("rabbitmq.update-product-queue"));
        log.info("  - delete.product → {}", env.getProperty("rabbitmq.delete-product-queue"));
        log.info("");
        log.info("COST CENTER QUEUES:");
        log.info("  - create.costCenter → {}", env.getProperty("rabbitmq.create-cost-center-queue"));
        log.info("  - update.costCenter → {}", env.getProperty("rabbitmq.update-cost-center-queue"));
        log.info("  - delete.costCenter → {}", env.getProperty("rabbitmq.delete-cost-center-queue"));
        log.info("");
        log.info("PROFIT CENTER QUEUES:");
        log.info("  - create.profitCenter → {}", env.getProperty("rabbitmq.create-profit-center-queue"));
        log.info("  - update.profitCenter → {}", env.getProperty("rabbitmq.update-profit-center-queue"));
        log.info("  - delete.profitCenter → {}", env.getProperty("rabbitmq.delete-profit-center-queue"));
        log.info("");
        log.info("STANDARD COST RATE QUEUES:");
        log.info("  - create.standardCostRate → {}", env.getProperty("rabbitmq.create-standard-cost-rate-queue"));
        log.info("  - update.standardCostRate → {}", env.getProperty("rabbitmq.update-standard-cost-rate-queue"));
        log.info("");
        log.info("COGS FORMULA QUEUES:");
        log.info("  - create.cogsFormula → {}", env.getProperty("rabbitmq.create-cogs-formula-queue"));
        log.info("  - update.cogsFormula → {}", env.getProperty("rabbitmq.update-cogs-formula-queue"));
        log.info("");
        log.info("PROFITABILITY ANALYSIS QUEUES:");
        log.info("  - create.profitabilityAnalysis → {}", env.getProperty("rabbitmq.create-profitability-analysis-queue"));
        log.info("  - update.profitabilityAnalysis → {}", env.getProperty("rabbitmq.update-profitability-analysis-queue"));
        log.info("");
        log.info("WITHHOLDING TAX RULE QUEUES:");
        log.info("  - create.withholdingTaxRule → {}", env.getProperty("rabbitmq.create-withholding-tax-rule-queue"));
        log.info("  - update.withholdingTaxRule → {}", env.getProperty("rabbitmq.update-withholding-tax-rule-queue"));
        log.info("");
        log.info("BUSINESS CALCULATION QUEUES:");
        log.info("  - cost.calculated → {}", env.getProperty("rabbitmq.cost-calculated-queue"));
        log.info("  - cogs.computed → {}", env.getProperty("rabbitmq.cogs-computed-queue"));
        log.info("");
        log.info("✅ CLEAN RESET COMPLETE - NO LEGACY BINDINGS");
        log.info("🔧 TOTAL CLEAN BINDINGS: 22");
        log.info("🎯 EXPECTED BEHAVIOR: Every publish → Queue delivery");
    }

    private void cleanupOldGenericQueues() {
        String[] oldGenericQueues = {"cost.create.queue", "cost.update.queue", "cost.delete.queue"};

        try (Connection connection = connectionFactory.createConnection();
             Channel channel = connection.createChannel(false)) {

            for (String queueName : oldGenericQueues) {
                try {
                    channel.queueDelete(queueName);
                    log.info("🗑️ Deleted old generic queue: {}", queueName);
                } catch (IOException e) {
                    // Queue might not exist, which is fine
                    log.info("ℹ️ Old generic queue {} does not exist or already deleted", queueName);
                }
            }
        } catch (Exception e) {
            log.error("❌ Error during cleanup of old generic queues: {}", e.getMessage(), e);
        }
    }
}
