package com.example.cost_service.mapper;

import com.example.cost_service.dto.request.WithholdingTaxRuleRequest;
import com.example.cost_service.dto.response.WithholdingTaxRuleResponse;
import com.example.cost_service.model.WithholdingTaxRule;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WithholdingTaxRuleMapper {

    public WithholdingTaxRule mapToEntity(WithholdingTaxRuleRequest request, UUID tenantId) {
        WithholdingTaxRule withholdingTaxRule = new WithholdingTaxRule();
        withholdingTaxRule.setTenantId(tenantId);
        withholdingTaxRule.setTaxName(request.getTaxName());
        withholdingTaxRule.setTaxType(request.getTaxType());
        withholdingTaxRule.setRate(request.getRate());
        withholdingTaxRule.setApplicableTo(request.getApplicableTo());
        return withholdingTaxRule;
    }

    public WithholdingTaxRuleResponse mapToDto(WithholdingTaxRule withholdingTaxRule) {
        WithholdingTaxRuleResponse response = new WithholdingTaxRuleResponse();
        response.setId(withholdingTaxRule.getId());
        response.setTenantId(withholdingTaxRule.getTenantId());
        response.setTaxName(withholdingTaxRule.getTaxName());
        response.setTaxType(withholdingTaxRule.getTaxType());
        response.setRate(withholdingTaxRule.getRate());
        response.setApplicableTo(withholdingTaxRule.getApplicableTo());
        response.setCreatedAt(withholdingTaxRule.getCreatedAt());
        response.setUpdatedAt(withholdingTaxRule.getUpdatedAt());
        response.setCreatedBy(withholdingTaxRule.getCreatedBy());
        response.setUpdatedBy(withholdingTaxRule.getUpdatedBy());
        return response;
    }

    public WithholdingTaxRule mapUpdateRequest(WithholdingTaxRule withholdingTaxRule, WithholdingTaxRuleRequest request) {
        withholdingTaxRule.setTaxName(request.getTaxName());
        withholdingTaxRule.setTaxType(request.getTaxType());
        withholdingTaxRule.setRate(request.getRate());
        withholdingTaxRule.setApplicableTo(request.getApplicableTo());
        return withholdingTaxRule;
    }
}
