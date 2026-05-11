package com.example.cost_service.service;

import com.example.cost_service.dto.eventDto.*;
import com.example.cost_service.event.CostEventProducer;
import com.example.cost_service.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CostEventService {

    private final CostEventProducer eventProducer;

    // PRODUCT EVENTS
    public void publishProductCreated(Product product) {
        ProductEventDto event = mapToProductEventDto(product);
        eventProducer.sendCreateProductEvent(event);
        log.info("Published product created event for tenant: {}, product: {}", 
                product.getTenantId(), product.getId());
    }

    public void publishProductUpdated(Product product) {
        ProductEventDto event = mapToProductEventDto(product);
        eventProducer.sendUpdateProductEvent(event);
        log.info("Published product updated event for tenant: {}, product: {}", 
                product.getTenantId(), product.getId());
    }

    public void publishProductDeleted(Product product) {
        ProductEventDto event = mapToProductEventDto(product);
        eventProducer.sendDeleteProductEvent(event);
        log.info("Published product deleted event for tenant: {}, product: {}", 
                product.getTenantId(), product.getId());
    }

    // COST CENTER EVENTS
    public void publishCostCenterCreated(CostCenter costCenter) {
        CostCenterEventDto event = mapToCostCenterEventDto(costCenter);
        eventProducer.sendCreateCostCenterEvent(event);
        log.info("Published cost center created event for tenant: {}, cost center: {}", 
                costCenter.getTenantId(), costCenter.getId());
    }

    public void publishCostCenterUpdated(CostCenter costCenter) {
        CostCenterEventDto event = mapToCostCenterEventDto(costCenter);
        eventProducer.sendUpdateCostCenterEvent(event);
        log.info("Published cost center updated event for tenant: {}, cost center: {}", 
                costCenter.getTenantId(), costCenter.getId());
    }

    public void publishCostCenterDeleted(CostCenter costCenter) {
        CostCenterEventDto event = mapToCostCenterEventDto(costCenter);
        eventProducer.sendDeleteCostCenterEvent(event);
        log.info("Published cost center deleted event for tenant: {}, cost center: {}", 
                costCenter.getTenantId(), costCenter.getId());
    }

    // PROFIT CENTER EVENTS
    public void publishProfitCenterCreated(ProfitCenter profitCenter) {
        ProfitCenterEventDto event = mapToProfitCenterEventDto(profitCenter);
        eventProducer.sendCreateProfitCenterEvent(event);
        log.info("Published profit center created event for tenant: {}, profit center: {}", 
                profitCenter.getTenantId(), profitCenter.getId());
    }

    public void publishProfitCenterUpdated(ProfitCenter profitCenter) {
        ProfitCenterEventDto event = mapToProfitCenterEventDto(profitCenter);
        eventProducer.sendUpdateProfitCenterEvent(event);
        log.info("Published profit center updated event for tenant: {}, profit center: {}", 
                profitCenter.getTenantId(), profitCenter.getId());
    }

    public void publishProfitCenterDeleted(ProfitCenter profitCenter) {
        ProfitCenterEventDto event = mapToProfitCenterEventDto(profitCenter);
        eventProducer.sendDeleteProfitCenterEvent(event);
        log.info("Published profit center deleted event for tenant: {}, profit center: {}", 
                profitCenter.getTenantId(), profitCenter.getId());
    }

    // COST RECORD EVENTS
    public void publishCostRecordCreated(CostRecord costRecord) {
        CostEventDto event = mapToCostEventDto(costRecord);
        eventProducer.sendCreateCostRecordEvent(event);
        log.info("Published cost record created event for tenant: {}, cost record: {}", 
                costRecord.getTenantId(), costRecord.getId());
    }

    public void publishCostRecordUpdated(CostRecord costRecord) {
        CostEventDto event = mapToCostEventDto(costRecord);
        eventProducer.sendUpdateCostRecordEvent(event);
        log.info("Published cost record updated event for tenant: {}, cost record: {}", 
                costRecord.getTenantId(), costRecord.getId());
    }

    public void publishCostRecordDeleted(CostRecord costRecord) {
        CostEventDto event = mapToCostEventDto(costRecord);
        eventProducer.sendDeleteCostRecordEvent(event);
        log.info("Published cost record deleted event for tenant: {}, cost record: {}", 
                costRecord.getTenantId(), costRecord.getId());
    }

    // STANDARD COST RATE EVENTS
    public void publishStandardCostRateCreated(StandardCostRate standardCostRate) {
        StandardCostRateEventDto event = mapToStandardCostRateEventDto(standardCostRate);
        eventProducer.sendCreateStandardCostRateEvent(event);
        log.info("Published standard cost rate created event for tenant: {}, rate: {}", 
                standardCostRate.getTenantId(), standardCostRate.getId());
    }

    public void publishStandardCostRateUpdated(StandardCostRate standardCostRate) {
        StandardCostRateEventDto event = mapToStandardCostRateEventDto(standardCostRate);
        eventProducer.sendUpdateStandardCostRateEvent(event);
        log.info("Published standard cost rate updated event for tenant: {}, rate: {}", 
                standardCostRate.getTenantId(), standardCostRate.getId());
    }

    // COGS FORMULA EVENTS
    public void publishCogsFormulaCreated(CogsFormula cogsFormula) {
        CogsFormulaEventDto event = mapToCogsFormulaEventDto(cogsFormula);
        eventProducer.sendCreateCogsFormulaEvent(event);
        log.info("Published COGS formula created event for tenant: {}, formula: {}", 
                cogsFormula.getTenantId(), cogsFormula.getId());
    }

    public void publishCogsFormulaUpdated(CogsFormula cogsFormula) {
        CogsFormulaEventDto event = mapToCogsFormulaEventDto(cogsFormula);
        eventProducer.sendUpdateCogsFormulaEvent(event);
        log.info("Published COGS formula updated event for tenant: {}, formula: {}", 
                cogsFormula.getTenantId(), cogsFormula.getId());
    }

    // PROFITABILITY ANALYSIS EVENTS
    public void publishProfitabilityAnalysisCreated(ProfitabilityAnalysis profitabilityAnalysis) {
        ProfitabilityAnalysisEventDto event = mapToProfitabilityAnalysisEventDto(profitabilityAnalysis);
        eventProducer.sendCreateProfitabilityAnalysisEvent(event);
        log.info("Published profitability analysis created event for tenant: {}, analysis: {}", 
                profitabilityAnalysis.getTenantId(), profitabilityAnalysis.getId());
    }

    public void publishProfitabilityAnalysisUpdated(ProfitabilityAnalysis profitabilityAnalysis) {
        ProfitabilityAnalysisEventDto event = mapToProfitabilityAnalysisEventDto(profitabilityAnalysis);
        eventProducer.sendUpdateProfitabilityAnalysisEvent(event);
        log.info("Published profitability analysis updated event for tenant: {}, analysis: {}", 
                profitabilityAnalysis.getTenantId(), profitabilityAnalysis.getId());
    }

    // WITHHOLDING TAX EVENTS
    public void publishWithholdingTaxRuleCreated(WithholdingTaxRule withholdingTaxRule) {
        WithholdingTaxRuleEventDto event = mapToWithholdingTaxRuleEventDto(withholdingTaxRule);
        eventProducer.sendCreateWithholdingTaxRuleEvent(event);
        log.info("Published withholding tax rule created event for tenant: {}, rule: {}", 
                withholdingTaxRule.getTenantId(), withholdingTaxRule.getId());
    }

    public void publishWithholdingTaxRuleUpdated(WithholdingTaxRule withholdingTaxRule) {
        WithholdingTaxRuleEventDto event = mapToWithholdingTaxRuleEventDto(withholdingTaxRule);
        eventProducer.sendUpdateWithholdingTaxRuleEvent(event);
        log.info("Published withholding tax rule updated event for tenant: {}, rule: {}", 
                withholdingTaxRule.getTenantId(), withholdingTaxRule.getId());
    }

    // COST CALCULATION EVENTS
    public void publishCostCalculated(UUID tenantId, UUID productId, UUID costCenterId, UUID periodId,
                                    java.math.BigDecimal materialCost, java.math.BigDecimal laborCost,
                                    java.math.BigDecimal overheadCost, java.math.BigDecimal totalCost,
                                    String calculatedBy, String calculationType) {
        CostCalculatedEventDto event = new CostCalculatedEventDto();
        event.setTenantId(tenantId);
        event.setProductId(productId);
        event.setCostCenterId(costCenterId);
        event.setPeriodId(periodId);
        event.setMaterialCost(materialCost);
        event.setLaborCost(laborCost);
        event.setOverheadCost(overheadCost);
        event.setTotalCost(totalCost);
        event.setCalculatedAt(LocalDateTime.now());
        event.setCalculatedBy(calculatedBy);
        event.setCalculationType(calculationType);
        
        eventProducer.sendCostCalculatedEvent(event);
        log.info("Published cost calculated event for tenant: {}, product: {}", tenantId, productId);
    }

    public void publishCogsComputed(UUID tenantId, UUID productId, UUID periodId,
                                  com.example.cost_service.enums.FormulaType formulaType,
                                  java.math.BigDecimal totalCogs,
                                  java.math.BigDecimal materialCost,
                                  java.math.BigDecimal laborCost,
                                  java.math.BigDecimal overheadCost,
                                  String computedBy, String formulaDescription) {
        CogsComputedEventDto event = new CogsComputedEventDto();
        event.setTenantId(tenantId);
        event.setProductId(productId);
        event.setPeriodId(periodId);
        event.setFormulaType(formulaType);
        event.setTotalCogs(totalCogs);
        event.setMaterialCost(materialCost);
        event.setLaborCost(laborCost);
        event.setOverheadCost(overheadCost);
        event.setComputedAt(LocalDateTime.now());
        event.setComputedBy(computedBy);
        event.setFormulaDescription(formulaDescription);
        
        eventProducer.sendCogsComputedEvent(event);
        log.info("Published COGS computed event for tenant: {}, product: {}", tenantId, productId);
    }

    // MAPPING METHODS
    private ProductEventDto mapToProductEventDto(Product product) {
        ProductEventDto event = new ProductEventDto();
        event.setId(product.getId());
        event.setTenantId(product.getTenantId());
        event.setProductCode(product.getProductCode());
        event.setName(product.getName());
        event.setCategory(product.getCategory());
        event.setIsActive(product.getIsActive());
        event.setCreatedAt(product.getCreatedAt());
        event.setUpdatedAt(product.getUpdatedAt());
        event.setCreatedBy(product.getCreatedBy());
        event.setUpdatedBy(product.getUpdatedBy());
        event.setCreatedByUsername(product.getCreatedByUsername());
        event.setUpdatedByUsername(product.getUpdatedByUsername());
        return event;
    }

    private CostCenterEventDto mapToCostCenterEventDto(CostCenter costCenter) {
        CostCenterEventDto event = new CostCenterEventDto();
        event.setId(costCenter.getId());
        event.setTenantId(costCenter.getTenantId());
        event.setCode(costCenter.getCode());
        event.setName(costCenter.getName());
        event.setType(costCenter.getType());
        event.setIsActive(costCenter.getIsActive());
        event.setProfitCenterId(costCenter.getProfitCenter() != null ? costCenter.getProfitCenter().getId() : null);
        event.setCreatedAt(costCenter.getCreatedAt());
        event.setUpdatedAt(costCenter.getUpdatedAt());
        event.setCreatedBy(costCenter.getCreatedBy());
        event.setUpdatedBy(costCenter.getUpdatedBy());
        event.setCreatedByUsername(costCenter.getCreatedByUsername());
        event.setUpdatedByUsername(costCenter.getUpdatedByUsername());
        return event;
    }

    private ProfitCenterEventDto mapToProfitCenterEventDto(ProfitCenter profitCenter) {
        ProfitCenterEventDto event = new ProfitCenterEventDto();
        event.setId(profitCenter.getId());
        event.setTenantId(profitCenter.getTenantId());
        event.setCode(profitCenter.getCode());
        event.setName(profitCenter.getName());
        event.setIsActive(profitCenter.getIsActive());
        event.setCreatedAt(profitCenter.getCreatedAt());
        event.setUpdatedAt(profitCenter.getUpdatedAt());
        event.setCreatedBy(profitCenter.getCreatedBy());
        event.setUpdatedBy(profitCenter.getUpdatedBy());
        event.setCreatedByUsername(profitCenter.getCreatedByUsername());
        event.setUpdatedByUsername(profitCenter.getUpdatedByUsername());
        return event;
    }

    private CostEventDto mapToCostEventDto(CostRecord costRecord) {
        CostEventDto event = new CostEventDto();
        event.setId(costRecord.getId());
        event.setTenantId(costRecord.getTenantId());
        event.setMaterialCost(costRecord.getMaterialCost());
        event.setLaborCost(costRecord.getLaborCost());
        event.setOverheadCost(costRecord.getOverheadCost());
        event.setTotalCost(costRecord.getTotalCost());
        event.setPeriodId(costRecord.getPeriodId());
        event.setProductId(costRecord.getProduct() != null ? costRecord.getProduct().getId() : null);
        event.setCostCenterId(costRecord.getCostCenter() != null ? costRecord.getCostCenter().getId() : null);
        event.setCreatedAt(costRecord.getCreatedAt());
        event.setUpdatedAt(costRecord.getUpdatedAt());
        event.setCreatedBy(costRecord.getCreatedBy());
        event.setUpdatedBy(costRecord.getUpdatedBy());
        event.setCreatedByUsername(costRecord.getCreatedByUsername());
        event.setUpdatedByUsername(costRecord.getUpdatedByUsername());
        return event;
    }

    private StandardCostRateEventDto mapToStandardCostRateEventDto(StandardCostRate standardCostRate) {
        StandardCostRateEventDto event = new StandardCostRateEventDto();
        event.setId(standardCostRate.getId());
        event.setTenantId(standardCostRate.getTenantId());
        event.setItemCode(standardCostRate.getItemCode());
        event.setRate(standardCostRate.getRate());
        event.setEffectiveDate(standardCostRate.getEffectiveDate());
        event.setProductId(standardCostRate.getProduct() != null ? standardCostRate.getProduct().getId() : null);
        event.setCreatedAt(standardCostRate.getCreatedAt());
        event.setUpdatedAt(standardCostRate.getUpdatedAt());
        event.setCreatedBy(standardCostRate.getCreatedBy());
        event.setUpdatedBy(standardCostRate.getUpdatedBy());
        event.setCreatedByUsername(standardCostRate.getCreatedByUsername());
        event.setUpdatedByUsername(standardCostRate.getUpdatedByUsername());
        return event;
    }

    private CogsFormulaEventDto mapToCogsFormulaEventDto(CogsFormula cogsFormula) {
        CogsFormulaEventDto event = new CogsFormulaEventDto();
        event.setId(cogsFormula.getId());
        event.setTenantId(cogsFormula.getTenantId());
        event.setFormulaName(cogsFormula.getFormulaName());
        event.setFormulaJson(cogsFormula.getFormulaJson());
        event.setPeriodId(cogsFormula.getPeriodId());
        event.setCreatedAt(cogsFormula.getCreatedAt());
        event.setUpdatedAt(cogsFormula.getUpdatedAt());
        event.setCreatedBy(cogsFormula.getCreatedBy());
        event.setUpdatedBy(cogsFormula.getUpdatedBy());
        event.setCreatedByUsername(cogsFormula.getCreatedByUsername());
        event.setUpdatedByUsername(cogsFormula.getUpdatedByUsername());
        return event;
    }

    private ProfitabilityAnalysisEventDto mapToProfitabilityAnalysisEventDto(ProfitabilityAnalysis profitabilityAnalysis) {
        ProfitabilityAnalysisEventDto event = new ProfitabilityAnalysisEventDto();
        event.setId(profitabilityAnalysis.getId());
        event.setTenantId(profitabilityAnalysis.getTenantId());
        event.setRevenue(profitabilityAnalysis.getRevenue());
        event.setCogs(profitabilityAnalysis.getCogs());
        event.setGrossProfit(profitabilityAnalysis.getGrossProfit());
        event.setAnalysisDate(profitabilityAnalysis.getAnalysisDate());
        event.setPeriodId(profitabilityAnalysis.getPeriodId());
        event.setProductId(profitabilityAnalysis.getProduct() != null ? profitabilityAnalysis.getProduct().getId() : null);
        event.setCostCenterId(profitabilityAnalysis.getCostCenter() != null ? profitabilityAnalysis.getCostCenter().getId() : null);
        event.setProfitCenterId(profitabilityAnalysis.getProfitCenter() != null ? profitabilityAnalysis.getProfitCenter().getId() : null);
        event.setCreatedAt(profitabilityAnalysis.getCreatedAt());
        event.setUpdatedAt(profitabilityAnalysis.getUpdatedAt());
        event.setCreatedBy(profitabilityAnalysis.getCreatedBy());
        event.setUpdatedBy(profitabilityAnalysis.getUpdatedBy());
        event.setCreatedByUsername(profitabilityAnalysis.getCreatedByUsername());
        event.setUpdatedByUsername(profitabilityAnalysis.getUpdatedByUsername());
        return event;
    }

    private WithholdingTaxRuleEventDto mapToWithholdingTaxRuleEventDto(WithholdingTaxRule withholdingTaxRule) {
        WithholdingTaxRuleEventDto event = new WithholdingTaxRuleEventDto();
        event.setId(withholdingTaxRule.getId());
        event.setTenantId(withholdingTaxRule.getTenantId());
        event.setTaxName(withholdingTaxRule.getTaxName());
        event.setTaxType(withholdingTaxRule.getTaxType());
        event.setRate(withholdingTaxRule.getRate());
        event.setApplicableTo(withholdingTaxRule.getApplicableTo());
        event.setCreatedAt(withholdingTaxRule.getCreatedAt());
        event.setUpdatedAt(withholdingTaxRule.getUpdatedAt());
        event.setCreatedBy(withholdingTaxRule.getCreatedBy());
        event.setUpdatedBy(withholdingTaxRule.getUpdatedBy());
        event.setCreatedByUsername(withholdingTaxRule.getCreatedByUsername());
        event.setUpdatedByUsername(withholdingTaxRule.getUpdatedByUsername());
        return event;
    }
}
