package com.example.cost_service.service;

import com.example.cost_service.dto.request.ProfitabilityAnalysisRequest;
import com.example.cost_service.dto.response.ProfitabilityAnalysisResponse;
import com.example.cost_service.exception.ResourceNotFoundException;
import com.example.cost_service.mapper.ProfitabilityAnalysisMapper;
import com.example.cost_service.model.CostCenter;
import com.example.cost_service.model.ProfitabilityAnalysis;
import com.example.cost_service.model.ProfitCenter;
import com.example.cost_service.model.Product;
import com.example.cost_service.repository.ProfitabilityAnalysisRepository;
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
public class ProfitabilityAnalysisService {

    private final ProfitabilityAnalysisRepository repository;
    private final ProfitabilityAnalysisMapper mapper;
    private final ValidationUtil validationUtil;
    private final SecurityUtil securityUtil;

    @Transactional
    public ProfitabilityAnalysisResponse addProfitabilityAnalysis(UUID tenantId, ProfitabilityAnalysisRequest request) {
        Product product = validationUtil.getProductById(tenantId, request.getProductId());
        CostCenter costCenter = validationUtil.getCostCenterById(tenantId, request.getCostCenterId());
        ProfitCenter profitCenter = validationUtil.getProfitCenterById(tenantId, request.getProfitCenterId());

        ProfitabilityAnalysis profitabilityAnalysis = mapper.mapToEntity(request, tenantId);
        profitabilityAnalysis.setProduct(product);
        profitabilityAnalysis.setCostCenter(costCenter);
        profitabilityAnalysis.setProfitCenter(profitCenter);
        
        // Set audit fields from JWT
        UUID userId = securityUtil.getUserId();
        String username = securityUtil.getUsername();
        if (userId != null) {
            profitabilityAnalysis.setCreatedBy(userId.toString());
            profitabilityAnalysis.setUpdatedBy(userId.toString());
            profitabilityAnalysis.setCreatedByUsername(username);
            profitabilityAnalysis.setUpdatedByUsername(username);
        } else {
            log.warn("Unable to extract user ID from JWT for audit fields");
        }
        
        ProfitabilityAnalysis savedProfitabilityAnalysis = repository.save(profitabilityAnalysis);
        log.info("Profitability analysis created with id: {}", savedProfitabilityAnalysis.getId());
        return mapper.mapToDto(savedProfitabilityAnalysis);
    }

    public List<ProfitabilityAnalysisResponse> getAllProfitabilityAnalyses(UUID tenantId) {
        return repository.findByTenantId(tenantId)
                .stream()
                .map(mapper::mapToDto)
                .toList();
    }

    public ProfitabilityAnalysisResponse getProfitabilityAnalysisById(UUID tenantId, UUID profitabilityAnalysisId) {
        ProfitabilityAnalysis profitabilityAnalysis = getById(tenantId, profitabilityAnalysisId);
        return mapper.mapToDto(profitabilityAnalysis);
    }

    @Transactional
    public ProfitabilityAnalysisResponse updateProfitabilityAnalysis(UUID tenantId, UUID profitabilityAnalysisId, ProfitabilityAnalysisRequest request) {
        ProfitabilityAnalysis profitabilityAnalysis = getById(tenantId, profitabilityAnalysisId);

        if (request.getProductId() != null && 
            (profitabilityAnalysis.getProduct() == null || 
             !profitabilityAnalysis.getProduct().getId().equals(request.getProductId()))) {
            Product product = validationUtil.getProductById(tenantId, request.getProductId());
            profitabilityAnalysis.setProduct(product);
        }

        if (request.getCostCenterId() != null && 
            (profitabilityAnalysis.getCostCenter() == null || 
             !profitabilityAnalysis.getCostCenter().getId().equals(request.getCostCenterId()))) {
            CostCenter costCenter = validationUtil.getCostCenterById(tenantId, request.getCostCenterId());
            profitabilityAnalysis.setCostCenter(costCenter);
        }

        if (request.getProfitCenterId() != null && 
            (profitabilityAnalysis.getProfitCenter() == null || 
             !profitabilityAnalysis.getProfitCenter().getId().equals(request.getProfitCenterId()))) {
            ProfitCenter profitCenter = validationUtil.getProfitCenterById(tenantId, request.getProfitCenterId());
            profitabilityAnalysis.setProfitCenter(profitCenter);
        }

        profitabilityAnalysis = mapper.mapUpdateRequest(profitabilityAnalysis, request);
        
        // Set updatedBy audit field from JWT
        UUID userId = securityUtil.getUserId();
        String username = securityUtil.getUsername();
        if (userId != null) {
            profitabilityAnalysis.setUpdatedBy(userId.toString());
            profitabilityAnalysis.setUpdatedByUsername(username);
        } else {
            log.warn("Unable to extract user ID from JWT for updatedBy audit field");
        }
        
        ProfitabilityAnalysis updatedProfitabilityAnalysis = repository.save(profitabilityAnalysis);
        log.info("Profitability analysis updated with id: {}", updatedProfitabilityAnalysis.getId());
        return mapper.mapToDto(updatedProfitabilityAnalysis);
    }

    @Transactional
    public void deleteProfitabilityAnalysis(UUID tenantId, UUID profitabilityAnalysisId) {
        ProfitabilityAnalysis profitabilityAnalysis = getById(tenantId, profitabilityAnalysisId);
        repository.delete(profitabilityAnalysis);
        log.info("Profitability analysis deleted with id: {}", profitabilityAnalysisId);
    }

    private ProfitabilityAnalysis getById(UUID tenantId, UUID id) {
        return repository.findById(id)
                .filter(e -> e.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Profitability analysis not found"));
    }
}
