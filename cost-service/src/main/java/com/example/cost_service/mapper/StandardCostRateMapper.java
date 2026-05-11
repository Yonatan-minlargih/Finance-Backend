package com.example.cost_service.mapper;

import com.example.cost_service.dto.request.StandardCostRateRequest;
import com.example.cost_service.dto.response.StandardCostRateResponse;
import com.example.cost_service.model.StandardCostRate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StandardCostRateMapper {

    public StandardCostRate mapToEntity(StandardCostRateRequest request, UUID tenantId) {
        StandardCostRate standardCostRate = new StandardCostRate();
        standardCostRate.setTenantId(tenantId);
        standardCostRate.setItemCode(request.getItemCode());
        standardCostRate.setRate(request.getRate());
        standardCostRate.setEffectiveDate(request.getEffectiveDate());
        return standardCostRate;
    }

    public StandardCostRateResponse mapToDto(StandardCostRate standardCostRate) {
        StandardCostRateResponse response = new StandardCostRateResponse();
        response.setId(standardCostRate.getId());
        response.setTenantId(standardCostRate.getTenantId());
        response.setProductId(standardCostRate.getProduct() != null ? standardCostRate.getProduct().getId() : null);
        response.setItemCode(standardCostRate.getItemCode());
        response.setRate(standardCostRate.getRate());
        response.setEffectiveDate(standardCostRate.getEffectiveDate());
        response.setCreatedAt(standardCostRate.getCreatedAt());
        response.setUpdatedAt(standardCostRate.getUpdatedAt());
        response.setCreatedBy(standardCostRate.getCreatedBy());
        response.setUpdatedBy(standardCostRate.getUpdatedBy());
        return response;
    }

    public StandardCostRate mapUpdateRequest(StandardCostRate standardCostRate, StandardCostRateRequest request) {
        standardCostRate.setItemCode(request.getItemCode());
        standardCostRate.setRate(request.getRate());
        standardCostRate.setEffectiveDate(request.getEffectiveDate());
        return standardCostRate;
    }
}
