package com.example.cost_service.mapper;

import com.example.cost_service.dto.request.CostCenterRequest;
import com.example.cost_service.dto.response.CostCenterResponse;
import com.example.cost_service.model.CostCenter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CostCenterMapper {

    public CostCenter mapToEntity(CostCenterRequest request, UUID tenantId) {
        CostCenter costCenter = new CostCenter();
        costCenter.setTenantId(tenantId);
        costCenter.setCode(request.getCode());
        costCenter.setName(request.getName());
        costCenter.setType(request.getType());
        costCenter.setIsActive(request.getIsActive());
        return costCenter;
    }

    public CostCenterResponse mapToDto(CostCenter costCenter) {
        CostCenterResponse response = new CostCenterResponse();
        response.setId(costCenter.getId());
        response.setTenantId(costCenter.getTenantId());
        response.setCode(costCenter.getCode());
        response.setName(costCenter.getName());
        response.setType(costCenter.getType());
        response.setIsActive(costCenter.getIsActive());
        response.setProfitCenterId(costCenter.getProfitCenter() != null ? costCenter.getProfitCenter().getId() : null);
        response.setCreatedAt(costCenter.getCreatedAt());
        response.setUpdatedAt(costCenter.getUpdatedAt());
        response.setCreatedBy(costCenter.getCreatedBy());
        response.setUpdatedBy(costCenter.getUpdatedBy());
        return response;
    }

    public CostCenter mapUpdateRequest(CostCenter costCenter, CostCenterRequest request) {
        costCenter.setCode(request.getCode());
        costCenter.setName(request.getName());
        costCenter.setType(request.getType());
        costCenter.setIsActive(request.getIsActive());
        return costCenter;
    }
}
