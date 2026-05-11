package com.example.cost_service.mapper;

import com.example.cost_service.dto.request.ProfitCenterRequest;
import com.example.cost_service.dto.response.ProfitCenterResponse;
import com.example.cost_service.model.ProfitCenter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProfitCenterMapper {

    public ProfitCenter mapToEntity(ProfitCenterRequest request, UUID tenantId) {
        ProfitCenter profitCenter = new ProfitCenter();
        profitCenter.setTenantId(tenantId);
        profitCenter.setCode(request.getCode());
        profitCenter.setName(request.getName());
        profitCenter.setIsActive(request.getIsActive());
        return profitCenter;
    }

    public ProfitCenterResponse mapToDto(ProfitCenter profitCenter) {
        ProfitCenterResponse response = new ProfitCenterResponse();
        response.setId(profitCenter.getId());
        response.setTenantId(profitCenter.getTenantId());
        response.setCode(profitCenter.getCode());
        response.setName(profitCenter.getName());
        response.setIsActive(profitCenter.getIsActive());
        response.setCreatedAt(profitCenter.getCreatedAt());
        response.setUpdatedAt(profitCenter.getUpdatedAt());
        response.setCreatedBy(profitCenter.getCreatedBy());
        response.setUpdatedBy(profitCenter.getUpdatedBy());
        return response;
    }

    public ProfitCenter mapUpdateRequest(ProfitCenter profitCenter, ProfitCenterRequest request) {
        profitCenter.setCode(request.getCode());
        profitCenter.setName(request.getName());
        profitCenter.setIsActive(request.getIsActive());
        return profitCenter;
    }
}
