package com.example.cost_service.mapper;

import com.example.cost_service.dto.request.ProfitabilityAnalysisRequest;
import com.example.cost_service.dto.response.ProfitabilityAnalysisResponse;
import com.example.cost_service.model.ProfitabilityAnalysis;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class ProfitabilityAnalysisMapper {

    public ProfitabilityAnalysis mapToEntity(ProfitabilityAnalysisRequest request, UUID tenantId) {
        ProfitabilityAnalysis profitabilityAnalysis = new ProfitabilityAnalysis();
        profitabilityAnalysis.setTenantId(tenantId);
        profitabilityAnalysis.setRevenue(request.getRevenue());
        profitabilityAnalysis.setCogs(request.getCogs());
        
        BigDecimal revenue = request.getRevenue();
        BigDecimal cogs = request.getCogs();
        if (revenue != null && cogs != null) {
            profitabilityAnalysis.setGrossProfit(revenue.subtract(cogs));
        } else {
            profitabilityAnalysis.setGrossProfit(BigDecimal.ZERO);
        }
        
        profitabilityAnalysis.setAnalysisDate(request.getAnalysisDate());
        profitabilityAnalysis.setPeriodId(request.getPeriodId());
        return profitabilityAnalysis;
    }

    public ProfitabilityAnalysisResponse mapToDto(ProfitabilityAnalysis profitabilityAnalysis) {
        ProfitabilityAnalysisResponse response = new ProfitabilityAnalysisResponse();
        response.setId(profitabilityAnalysis.getId());
        response.setTenantId(profitabilityAnalysis.getTenantId());
        response.setProductId(profitabilityAnalysis.getProduct() != null ? profitabilityAnalysis.getProduct().getId() : null);
        response.setCostCenterId(profitabilityAnalysis.getCostCenter() != null ? profitabilityAnalysis.getCostCenter().getId() : null);
        response.setProfitCenterId(profitabilityAnalysis.getProfitCenter() != null ? profitabilityAnalysis.getProfitCenter().getId() : null);
        response.setRevenue(profitabilityAnalysis.getRevenue());
        response.setCogs(profitabilityAnalysis.getCogs());
        response.setGrossProfit(profitabilityAnalysis.getGrossProfit());
        response.setAnalysisDate(profitabilityAnalysis.getAnalysisDate());
        response.setPeriodId(profitabilityAnalysis.getPeriodId());
        response.setCreatedAt(profitabilityAnalysis.getCreatedAt());
        response.setUpdatedAt(profitabilityAnalysis.getUpdatedAt());
        response.setCreatedBy(profitabilityAnalysis.getCreatedBy());
        response.setUpdatedBy(profitabilityAnalysis.getUpdatedBy());
        return response;
    }

    public ProfitabilityAnalysis mapUpdateRequest(ProfitabilityAnalysis profitabilityAnalysis, ProfitabilityAnalysisRequest request) {
        profitabilityAnalysis.setRevenue(request.getRevenue());
        profitabilityAnalysis.setCogs(request.getCogs());
        
        BigDecimal revenue = request.getRevenue();
        BigDecimal cogs = request.getCogs();
        if (revenue != null && cogs != null) {
            profitabilityAnalysis.setGrossProfit(revenue.subtract(cogs));
        } else {
            profitabilityAnalysis.setGrossProfit(BigDecimal.ZERO);
        }
        
        profitabilityAnalysis.setAnalysisDate(request.getAnalysisDate());
        profitabilityAnalysis.setPeriodId(request.getPeriodId());
        return profitabilityAnalysis;
    }
}
