package com.example.cost_service.service;

import com.example.cost_service.dto.eventDto.CostEventDto;
import com.example.cost_service.dto.request.CostRecordRequest;
import com.example.cost_service.dto.response.CostRecordResponse;
import com.example.cost_service.exception.ResourceNotFoundException;
import com.example.cost_service.mapper.CostRecordMapper;
import com.example.cost_service.model.CostCenter;
import com.example.cost_service.model.CostRecord;
import com.example.cost_service.model.Product;
import com.example.cost_service.repository.CostRecordRepository;
import com.example.cost_service.utility.SecurityUtil;
import com.example.cost_service.utility.ValidationUtil;
import com.example.cost_service.event.CostEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CostRecordService {

    private final CostRecordRepository repository;
    private final CostRecordMapper mapper;
    private final ValidationUtil validationUtil;
    private final SecurityUtil securityUtil;
    private final CostEventProducer eventProducer;

    @Transactional
    public CostRecordResponse addCostRecord(UUID tenantId, CostRecordRequest request) {
        Product product = validationUtil.getProductById(tenantId, request.getProductId());
        CostCenter costCenter = validationUtil.getCostCenterById(tenantId, request.getCostCenterId());
        validationUtil.getPeriodById(tenantId, request.getPeriodId());

        CostRecord costRecord = mapper.mapToEntity(request, tenantId);
        costRecord.setProduct(product);
        costRecord.setCostCenter(costCenter);
        
        // Set audit fields from JWT
        UUID userId = securityUtil.getUserId();
        String username = securityUtil.getUsername();
        if (userId != null) {
            costRecord.setCreatedBy(userId.toString());
            costRecord.setUpdatedBy(userId.toString());
            costRecord.setCreatedByUsername(username);
            costRecord.setUpdatedByUsername(username);
        } else {
            log.warn("Unable to extract user ID from JWT for audit fields");
        }
        
        CostRecord savedCostRecord = repository.save(costRecord);
        log.info("Cost record created with id: {}", savedCostRecord.getId());
        log.info("🔍 STEP 1: CostRecord entity saved successfully. Preparing to publish event. EntityId={}, TenantId={}", 
                savedCostRecord.getId(), savedCostRecord.getTenantId());
        
        // Publish create event
        CostEventDto event = mapToEventDto(savedCostRecord);
        eventProducer.sendCreateCostRecordEvent(event);
        
        return mapper.mapToDto(savedCostRecord);
    }

    public List<CostRecordResponse> getAllCostRecords(UUID tenantId) {
        return repository.findByTenantId(tenantId)
                .stream()
                .map(mapper::mapToDto)
                .toList();
    }

    public CostRecordResponse getCostRecordById(UUID tenantId, UUID costRecordId) {
        CostRecord costRecord = getById(tenantId, costRecordId);
        return mapper.mapToDto(costRecord);
    }

    @Transactional
    public CostRecordResponse updateCostRecord(UUID tenantId, UUID costRecordId, CostRecordRequest request) {
        CostRecord costRecord = getById(tenantId, costRecordId);

        if (request.getProductId() != null && 
            (costRecord.getProduct() == null || 
             !costRecord.getProduct().getId().equals(request.getProductId()))) {
            Product product = validationUtil.getProductById(tenantId, request.getProductId());
            costRecord.setProduct(product);
        }

        if (request.getCostCenterId() != null && 
            (costRecord.getCostCenter() == null || 
             !costRecord.getCostCenter().getId().equals(request.getCostCenterId()))) {
            CostCenter costCenter = validationUtil.getCostCenterById(tenantId, request.getCostCenterId());
            costRecord.setCostCenter(costCenter);
        }

        if (request.getPeriodId() != null && !request.getPeriodId().equals(costRecord.getPeriodId())) {
            validationUtil.getPeriodById(tenantId, request.getPeriodId());
        }

        costRecord = mapper.mapUpdateRequest(costRecord, request);
        
        // Set updatedBy audit field from JWT
        UUID userId = securityUtil.getUserId();
        String username = securityUtil.getUsername();
        if (userId != null) {
            costRecord.setUpdatedBy(userId.toString());
            costRecord.setUpdatedByUsername(username);
        } else {
            log.warn("Unable to extract user ID from JWT for updatedBy audit field");
        }
        
        CostRecord updatedCostRecord = repository.save(costRecord);
        log.info("Cost record updated with id: {}", updatedCostRecord.getId());
        log.info("🔍 STEP 1: CostRecord entity updated successfully. Preparing to publish event. EntityId={}, TenantId={}", 
                updatedCostRecord.getId(), updatedCostRecord.getTenantId());
        
        // Publish update event
        CostEventDto event = mapToEventDto(updatedCostRecord);
        eventProducer.sendUpdateCostRecordEvent(event);
        
        return mapper.mapToDto(updatedCostRecord);
    }

    @Transactional
    public void deleteCostRecord(UUID tenantId, UUID costRecordId) {
        CostRecord costRecord = getById(tenantId, costRecordId);
        log.info("🔍 STEP 1: CostRecord entity retrieved for deletion. Preparing to publish event. EntityId={}, TenantId={}", 
                costRecord.getId(), costRecord.getTenantId());
        
        // Publish delete event before deletion
        CostEventDto event = mapToEventDto(costRecord);
        eventProducer.sendDeleteCostRecordEvent(event);
        
        repository.delete(costRecord);
        log.info("Cost record deleted with id: {}", costRecordId);
    }

    private CostRecord getById(UUID tenantId, UUID id) {
        return repository.findById(id)
                .filter(e -> e.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Cost record not found"));
    }

    private CostEventDto mapToEventDto(CostRecord costRecord) {
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
}
