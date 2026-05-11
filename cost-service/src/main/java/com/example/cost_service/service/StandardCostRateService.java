package com.example.cost_service.service;

import com.example.cost_service.dto.request.StandardCostRateRequest;
import com.example.cost_service.dto.response.StandardCostRateResponse;
import com.example.cost_service.exception.DuplicateResourceException;
import com.example.cost_service.exception.ResourceNotFoundException;
import com.example.cost_service.mapper.StandardCostRateMapper;
import com.example.cost_service.model.Product;
import com.example.cost_service.model.StandardCostRate;
import com.example.cost_service.repository.StandardCostRateRepository;
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
public class StandardCostRateService {

    private final StandardCostRateRepository repository;
    private final StandardCostRateMapper mapper;
    private final ValidationUtil validationUtil;
    private final SecurityUtil securityUtil;
    private final CostEventService eventService;

    @Transactional
    public StandardCostRateResponse addStandardCostRate(UUID tenantId, StandardCostRateRequest request) {
        // Check for duplicate item code within the same tenant
        List<StandardCostRate> existingRates = repository.findByTenantIdAndItemCode(tenantId, request.getItemCode());
        if (!existingRates.isEmpty()) {
            throw new DuplicateResourceException("Standard cost rate with this item code already exists");
        }
        
        Product product = validationUtil.getProductById(tenantId, request.getProductId());

        StandardCostRate standardCostRate = mapper.mapToEntity(request, tenantId);
        standardCostRate.setProduct(product);
        
        // Set audit fields from JWT
        UUID userId = securityUtil.getUserId();
        String username = securityUtil.getUsername();
        if (userId != null) {
            standardCostRate.setCreatedBy(userId.toString());
            standardCostRate.setUpdatedBy(userId.toString());
            standardCostRate.setCreatedByUsername(username);
            standardCostRate.setUpdatedByUsername(username);
        } else {
            log.warn("Unable to extract user ID from JWT for audit fields");
        }
        
        StandardCostRate savedStandardCostRate = repository.save(standardCostRate);
        log.info("Standard cost rate created with id: {}", savedStandardCostRate.getId());
        
        // Publish create event
        eventService.publishStandardCostRateCreated(savedStandardCostRate);
        
        return mapper.mapToDto(savedStandardCostRate);
    }

    public List<StandardCostRateResponse> getAllStandardCostRates(UUID tenantId) {
        return repository.findByTenantId(tenantId)
                .stream()
                .map(mapper::mapToDto)
                .toList();
    }

    public StandardCostRateResponse getStandardCostRateById(UUID tenantId, UUID standardCostRateId) {
        StandardCostRate standardCostRate = getById(tenantId, standardCostRateId);
        return mapper.mapToDto(standardCostRate);
    }

    @Transactional
    public StandardCostRateResponse updateStandardCostRate(UUID tenantId, UUID standardCostRateId, StandardCostRateRequest request) {
        StandardCostRate standardCostRate = getById(tenantId, standardCostRateId);

        // Check for duplicate item code within the same tenant (excluding current record)
        if (request.getItemCode() != null && !request.getItemCode().equals(standardCostRate.getItemCode())) {
            List<StandardCostRate> existingRates = repository.findByTenantIdAndItemCode(tenantId, request.getItemCode());
            if (!existingRates.isEmpty()) {
                throw new DuplicateResourceException("Standard cost rate with this item code already exists");
            }
        }

        if (request.getProductId() != null && 
            (standardCostRate.getProduct() == null || 
             !standardCostRate.getProduct().getId().equals(request.getProductId()))) {
            Product product = validationUtil.getProductById(tenantId, request.getProductId());
            standardCostRate.setProduct(product);
        }

        standardCostRate = mapper.mapUpdateRequest(standardCostRate, request);
        
        // Set updatedBy audit field from JWT
        UUID userId = securityUtil.getUserId();
        String username = securityUtil.getUsername();
        if (userId != null) {
            standardCostRate.setUpdatedBy(userId.toString());
            standardCostRate.setUpdatedByUsername(username);
        } else {
            log.warn("Unable to extract user ID from JWT for updatedBy audit field");
        }
        
        StandardCostRate updatedStandardCostRate = repository.save(standardCostRate);
        log.info("Standard cost rate updated with id: {}", updatedStandardCostRate.getId());
        
        // Publish update event
        eventService.publishStandardCostRateUpdated(updatedStandardCostRate);
        
        return mapper.mapToDto(updatedStandardCostRate);
    }

    @Transactional
    public void deleteStandardCostRate(UUID tenantId, UUID standardCostRateId) {
        StandardCostRate standardCostRate = getById(tenantId, standardCostRateId);
        repository.delete(standardCostRate);
        log.info("Standard cost rate deleted with id: {}", standardCostRateId);
    }

    private StandardCostRate getById(UUID tenantId, UUID id) {
        return repository.findById(id)
                .filter(e -> e.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Standard cost rate not found"));
    }
}
