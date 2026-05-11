package com.example.cost_service.service;

import com.example.cost_service.dto.request.ProfitCenterRequest;
import com.example.cost_service.dto.response.ProfitCenterResponse;
import com.example.cost_service.exception.DuplicateResourceException;
import com.example.cost_service.mapper.ProfitCenterMapper;
import com.example.cost_service.model.ProfitCenter;
import com.example.cost_service.repository.ProfitCenterRepository;
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
public class ProfitCenterService {

    private final ProfitCenterRepository repository;
    private final ProfitCenterMapper mapper;
    private final ValidationUtil validationUtil;
    private final SecurityUtil securityUtil;
    private final CostEventService eventService;

    @Transactional
    public ProfitCenterResponse addProfitCenter(UUID tenantId, ProfitCenterRequest request) {
        if (repository.existsByTenantIdAndCode(tenantId, request.getCode())) {
            throw new DuplicateResourceException("Profit center with this code already exists");
        }

        ProfitCenter profitCenter = mapper.mapToEntity(request, tenantId);
        
        // Set audit fields from JWT
        UUID userId = securityUtil.getUserId();
        String username = securityUtil.getUsername();
        if (userId != null) {
            profitCenter.setCreatedBy(userId.toString());
            profitCenter.setUpdatedBy(userId.toString());
            profitCenter.setCreatedByUsername(username);
            profitCenter.setUpdatedByUsername(username);
        } else {
            log.warn("Unable to extract user ID from JWT for audit fields");
        }
        
        ProfitCenter savedProfitCenter = repository.save(profitCenter);
        log.info("Profit center created with id: {}", savedProfitCenter.getId());
        
        // Publish create event
        eventService.publishProfitCenterCreated(savedProfitCenter);
        
        return mapper.mapToDto(savedProfitCenter);
    }

    public List<ProfitCenterResponse> getAllProfitCenters(UUID tenantId) {
        return repository.findByTenantId(tenantId)
                .stream()
                .map(mapper::mapToDto)
                .toList();
    }

    public ProfitCenterResponse getProfitCenterById(UUID tenantId, UUID profitCenterId) {
        ProfitCenter profitCenter = validationUtil.getProfitCenterById(tenantId, profitCenterId);
        return mapper.mapToDto(profitCenter);
    }

    @Transactional
    public ProfitCenterResponse updateProfitCenter(UUID tenantId, UUID profitCenterId, ProfitCenterRequest request) {
        ProfitCenter profitCenter = validationUtil.getProfitCenterById(tenantId, profitCenterId);
        
        if (repository.existsByTenantIdAndCodeAndIdNot(tenantId, request.getCode(), profitCenterId)) {
            throw new DuplicateResourceException("Profit center with this code already exists");
        }

        profitCenter = mapper.mapUpdateRequest(profitCenter, request);
        
        // Set updatedBy audit field from JWT
        UUID userId = securityUtil.getUserId();
        String username = securityUtil.getUsername();
        if (userId != null) {
            profitCenter.setUpdatedBy(userId.toString());
            profitCenter.setUpdatedByUsername(username);
        } else {
            log.warn("Unable to extract user ID from JWT for updatedBy audit field");
        }
        
        ProfitCenter updatedProfitCenter = repository.save(profitCenter);
        log.info("Profit center updated with id: {}", updatedProfitCenter.getId());
        return mapper.mapToDto(updatedProfitCenter);
    }

    @Transactional
    public void deleteProfitCenter(UUID tenantId, UUID profitCenterId) {
        ProfitCenter profitCenter = validationUtil.getProfitCenterById(tenantId, profitCenterId);
        
        // Publish delete event before deletion
        eventService.publishProfitCenterDeleted(profitCenter);
        
        repository.delete(profitCenter);
        log.info("Profit center deleted with id: {}", profitCenterId);
    }
}
