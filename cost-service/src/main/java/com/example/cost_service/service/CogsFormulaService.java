package com.example.cost_service.service;

import com.example.cost_service.dto.request.CogsFormulaRequest;
import com.example.cost_service.dto.response.CogsFormulaResponse;
import com.example.cost_service.mapper.CogsFormulaMapper;
import com.example.cost_service.model.CogsFormula;
import com.example.cost_service.repository.CogsFormulaRepository;
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
public class CogsFormulaService {

    private final CogsFormulaRepository repository;
    private final CogsFormulaMapper mapper;
    private final ValidationUtil validationUtil;
    private final SecurityUtil securityUtil;

    @Transactional
    public CogsFormulaResponse addCogsFormula(UUID tenantId, CogsFormulaRequest request) {
        CogsFormula cogsFormula = mapper.mapToEntity(request, tenantId);
        
        // Set audit fields from JWT
        UUID userId = securityUtil.getUserId();
        String username = securityUtil.getUsername();
        if (userId != null) {
            cogsFormula.setCreatedBy(userId.toString());
            cogsFormula.setUpdatedBy(userId.toString());
            cogsFormula.setCreatedByUsername(username);
            cogsFormula.setUpdatedByUsername(username);
        } else {
            log.warn("Unable to extract user ID from JWT for audit fields");
        }
        
        CogsFormula savedCogsFormula = repository.save(cogsFormula);
        log.info("COGS formula created with id: {}", savedCogsFormula.getId());
        return mapper.mapToDto(savedCogsFormula);
    }

    public List<CogsFormulaResponse> getAllCogsFormulas(UUID tenantId) {
        return repository.findByTenantId(tenantId)
                .stream()
                .map(mapper::mapToDto)
                .toList();
    }

    public CogsFormulaResponse getCogsFormulaById(UUID tenantId, UUID cogsFormulaId) {
        CogsFormula cogsFormula = validationUtil.getCogsFormulaById(tenantId, cogsFormulaId);
        return mapper.mapToDto(cogsFormula);
    }

    @Transactional
    public CogsFormulaResponse updateCogsFormula(UUID tenantId, UUID cogsFormulaId, CogsFormulaRequest request) {
        CogsFormula cogsFormula = validationUtil.getCogsFormulaById(tenantId, cogsFormulaId);
        cogsFormula = mapper.mapUpdateRequest(cogsFormula, request);
        
        // Set updatedBy audit field from JWT
        UUID userId = securityUtil.getUserId();
        String username = securityUtil.getUsername();
        if (userId != null) {
            cogsFormula.setUpdatedBy(userId.toString());
            cogsFormula.setUpdatedByUsername(username);
        } else {
            log.warn("Unable to extract user ID from JWT for updatedBy audit field");
        }
        
        CogsFormula updatedCogsFormula = repository.save(cogsFormula);
        log.info("COGS formula updated with id: {}", updatedCogsFormula.getId());
        return mapper.mapToDto(updatedCogsFormula);
    }

    @Transactional
    public void deleteCogsFormula(UUID tenantId, UUID cogsFormulaId) {
        CogsFormula cogsFormula = validationUtil.getCogsFormulaById(tenantId, cogsFormulaId);
        repository.delete(cogsFormula);
        log.info("COGS formula deleted with id: {}", cogsFormulaId);
    }
}
