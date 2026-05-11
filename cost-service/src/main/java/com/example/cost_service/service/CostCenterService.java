package com.example.cost_service.service;

import com.example.cost_service.dto.request.CostCenterRequest;
import com.example.cost_service.dto.response.CostCenterResponse;
import com.example.cost_service.exception.ResourceNotFoundException;
import com.example.cost_service.mapper.CostCenterMapper;
import com.example.cost_service.model.CostCenter;
import com.example.cost_service.model.ProfitCenter;
import com.example.cost_service.repository.CostCenterRepository;
import com.example.cost_service.utility.SecurityUtil;
import com.example.cost_service.utility.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CostCenterService {

    private final CostCenterRepository repository;
    private final CostCenterMapper mapper;
    private final ValidationUtil validationUtil;
    private final SecurityUtil securityUtil;
    private final CostEventService eventService;

    @Transactional
    public CostCenterResponse addCostCenter(UUID tenantId, CostCenterRequest request) {
        ProfitCenter profitCenter = validationUtil.getProfitCenterById(tenantId, request.getProfitCenterId());

        CostCenter costCenter = mapper.mapToEntity(request, tenantId);
        costCenter.setProfitCenter(profitCenter);
        
        // Set audit fields from JWT
        UUID userId = securityUtil.getUserId();
        String username = securityUtil.getUsername();
        if (userId != null) {
            costCenter.setCreatedBy(userId.toString());
            costCenter.setUpdatedBy(userId.toString());
            costCenter.setCreatedByUsername(username);
            costCenter.setUpdatedByUsername(username);
        } else {
            log.warn("Unable to extract user ID from JWT for audit fields");
        }
        
        CostCenter savedCostCenter = repository.save(costCenter);
        log.info("Cost center created with id: {}", savedCostCenter.getId());
        
        // Publish create event
        eventService.publishCostCenterCreated(savedCostCenter);
        
        return mapper.mapToDto(savedCostCenter);
    }

    public List<CostCenterResponse> getAllCostCenters(UUID tenantId) {
        return repository.findByTenantId(tenantId)
                .stream()
                .map(mapper::mapToDto)
                .toList();
    }

    public CostCenterResponse getCostCenterById(UUID tenantId, UUID costCenterId) {
        CostCenter costCenter = getById(tenantId, costCenterId);
        return mapper.mapToDto(costCenter);
    }

    @Transactional
    public CostCenterResponse updateCostCenter(UUID tenantId, UUID costCenterId, CostCenterRequest request) {
        CostCenter costCenter = getById(tenantId, costCenterId);

        if (request.getProfitCenterId() != null && 
            (costCenter.getProfitCenter() == null || 
             !costCenter.getProfitCenter().getId().equals(request.getProfitCenterId()))) {
            ProfitCenter profitCenter = validationUtil.getProfitCenterById(tenantId, request.getProfitCenterId());
            costCenter.setProfitCenter(profitCenter);
        }

        costCenter = mapper.mapUpdateRequest(costCenter, request);
        
        // Set updatedBy audit field from JWT
        UUID userId = securityUtil.getUserId();
        String username = securityUtil.getUsername();
        if (userId != null) {
            costCenter.setUpdatedBy(userId.toString());
            costCenter.setUpdatedByUsername(username);
        } else {
            log.warn("Unable to extract user ID from JWT for updatedBy audit field");
        }
        
        CostCenter updatedCostCenter = repository.save(costCenter);
        log.info("Cost center updated with id: {}", updatedCostCenter.getId());
        
        // Publish update event
        eventService.publishCostCenterUpdated(updatedCostCenter);
        
        return mapper.mapToDto(updatedCostCenter);
    }

    @Transactional
    public void deleteCostCenter(UUID tenantId, UUID costCenterId) {
        CostCenter costCenter = getById(tenantId, costCenterId);
        
        // Publish delete event before deletion
        eventService.publishCostCenterDeleted(costCenter);
        
        repository.delete(costCenter);
        log.info("Cost center deleted with id: {}", costCenterId);
    }

    private CostCenter getById(UUID tenantId, UUID id) {
        return repository.findById(id)
                .filter(e -> e.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Cost center not found"));
    }
}
