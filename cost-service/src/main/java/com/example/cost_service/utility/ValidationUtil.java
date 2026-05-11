package com.example.cost_service.utility;

import com.example.cost_service.client.PeriodServiceClient;
import com.example.cost_service.dto.clientDto.PeriodDto;
import com.example.cost_service.exception.ResourceNotFoundException;
import com.example.cost_service.model.*;
import com.example.cost_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationUtil {

    private final ProductRepository productRepository;
    private final ProfitCenterRepository profitCenterRepository;
    private final CostCenterRepository costCenterRepository;
    private final CogsFormulaRepository cogsFormulaRepository;
    private final WithholdingTaxRuleRepository withholdingTaxRuleRepository;
    private final PeriodServiceClient periodServiceClient;

    public Product getProductById(UUID tenantId, UUID productId) {
        return productRepository.findById(productId)
                .filter(product -> product.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    public ProfitCenter getProfitCenterById(UUID tenantId, UUID profitCenterId) {
        return profitCenterRepository.findById(profitCenterId)
                .filter(profitCenter -> profitCenter.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Profit center not found"));
    }

    public CostCenter getCostCenterById(UUID tenantId, UUID costCenterId) {
        return costCenterRepository.findById(costCenterId)
                .filter(costCenter -> costCenter.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Cost center not found"));
    }

    public CogsFormula getCogsFormulaById(UUID tenantId, UUID cogsFormulaId) {
        return cogsFormulaRepository.findById(cogsFormulaId)
                .filter(cogsFormula -> cogsFormula.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("COGS formula not found"));
    }

    public WithholdingTaxRule getWithholdingTaxRuleById(UUID tenantId, UUID withholdingTaxRuleId) {
        return withholdingTaxRuleRepository.findById(withholdingTaxRuleId)
                .filter(withholdingTaxRule -> withholdingTaxRule.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Withholding tax rule not found"));
    }

    public PeriodDto getPeriodById(UUID tenantId, UUID periodId) {
        try {
            PeriodDto period = periodServiceClient.getPeriodById(periodId);
            if (period == null || !period.getTenantId().equals(tenantId.toString())) {
                throw new ResourceNotFoundException("Period not found for this tenant");
            }
            return period;
        } catch (Exception e) {
            log.error("Error validating period {}: {}", periodId, e.getMessage());
            throw new ResourceNotFoundException("Period validation failed: " + e.getMessage());
        }
    }
}
