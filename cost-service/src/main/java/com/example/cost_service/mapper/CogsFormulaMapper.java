package com.example.cost_service.mapper;

import com.example.cost_service.dto.request.CogsFormulaRequest;
import com.example.cost_service.dto.response.CogsFormulaResponse;
import com.example.cost_service.model.CogsFormula;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CogsFormulaMapper {

    public CogsFormula mapToEntity(CogsFormulaRequest request, UUID tenantId) {
        CogsFormula cogsFormula = new CogsFormula();
        cogsFormula.setTenantId(tenantId);
        cogsFormula.setFormulaName(request.getFormulaName());
        cogsFormula.setFormulaJson(request.getFormulaJson());
        cogsFormula.setPeriodId(request.getPeriodId());
        return cogsFormula;
    }

    public CogsFormulaResponse mapToDto(CogsFormula cogsFormula) {
        CogsFormulaResponse response = new CogsFormulaResponse();
        response.setId(cogsFormula.getId());
        response.setTenantId(cogsFormula.getTenantId());
        response.setFormulaName(cogsFormula.getFormulaName());
        response.setFormulaJson(cogsFormula.getFormulaJson());
        response.setPeriodId(cogsFormula.getPeriodId());
        response.setCreatedAt(cogsFormula.getCreatedAt());
        response.setUpdatedAt(cogsFormula.getUpdatedAt());
        response.setCreatedBy(cogsFormula.getCreatedBy());
        response.setUpdatedBy(cogsFormula.getUpdatedBy());
        return response;
    }

    public CogsFormula mapUpdateRequest(CogsFormula cogsFormula, CogsFormulaRequest request) {
        cogsFormula.setFormulaName(request.getFormulaName());
        cogsFormula.setFormulaJson(request.getFormulaJson());
        cogsFormula.setPeriodId(request.getPeriodId());
        return cogsFormula;
    }
}
