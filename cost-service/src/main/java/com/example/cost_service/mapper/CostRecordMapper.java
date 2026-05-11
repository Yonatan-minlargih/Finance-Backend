package com.example.cost_service.mapper;

import com.example.cost_service.dto.request.CostRecordRequest;
import com.example.cost_service.dto.response.CostRecordResponse;
import com.example.cost_service.model.CostRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CostRecordMapper {

    public CostRecord mapToEntity(CostRecordRequest request, UUID tenantId) {
        CostRecord costRecord = new CostRecord();
        costRecord.setTenantId(tenantId);
        costRecord.setMaterialCost(request.getMaterialCost());
        costRecord.setLaborCost(request.getLaborCost());
        costRecord.setOverheadCost(request.getOverheadCost());
        
        BigDecimal materialCost = request.getMaterialCost();
        BigDecimal laborCost = request.getLaborCost();
        BigDecimal overheadCost = request.getOverheadCost();
        
        BigDecimal totalCost = BigDecimal.ZERO;
        if (materialCost != null) {
            totalCost = totalCost.add(materialCost);
        }
        if (laborCost != null) {
            totalCost = totalCost.add(laborCost);
        }
        if (overheadCost != null) {
            totalCost = totalCost.add(overheadCost);
        }
        costRecord.setTotalCost(totalCost);
        
        costRecord.setPeriodId(request.getPeriodId());
        return costRecord;
    }

    public CostRecordResponse mapToDto(CostRecord costRecord) {
        CostRecordResponse response = new CostRecordResponse();
        response.setId(costRecord.getId());
        response.setTenantId(costRecord.getTenantId());
        response.setProductId(costRecord.getProduct() != null ? costRecord.getProduct().getId() : null);
        response.setCostCenterId(costRecord.getCostCenter() != null ? costRecord.getCostCenter().getId() : null);
        response.setMaterialCost(costRecord.getMaterialCost());
        response.setLaborCost(costRecord.getLaborCost());
        response.setOverheadCost(costRecord.getOverheadCost());
        response.setTotalCost(costRecord.getTotalCost());
        response.setPeriodId(costRecord.getPeriodId());
        response.setCreatedAt(costRecord.getCreatedAt());
        response.setUpdatedAt(costRecord.getUpdatedAt());
        response.setCreatedBy(costRecord.getCreatedBy());
        response.setUpdatedBy(costRecord.getUpdatedBy());
        return response;
    }

    public CostRecord mapUpdateRequest(CostRecord costRecord, CostRecordRequest request) {
        costRecord.setMaterialCost(request.getMaterialCost());
        costRecord.setLaborCost(request.getLaborCost());
        costRecord.setOverheadCost(request.getOverheadCost());
        
        BigDecimal materialCost = request.getMaterialCost();
        BigDecimal laborCost = request.getLaborCost();
        BigDecimal overheadCost = request.getOverheadCost();
        
        BigDecimal totalCost = BigDecimal.ZERO;
        if (materialCost != null) {
            totalCost = totalCost.add(materialCost);
        }
        if (laborCost != null) {
            totalCost = totalCost.add(laborCost);
        }
        if (overheadCost != null) {
            totalCost = totalCost.add(overheadCost);
        }
        costRecord.setTotalCost(totalCost);
        
        costRecord.setPeriodId(request.getPeriodId());
        return costRecord;
    }
}
