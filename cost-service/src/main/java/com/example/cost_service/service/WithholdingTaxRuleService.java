package com.example.cost_service.service;

import com.example.cost_service.dto.request.WithholdingTaxRuleRequest;
import com.example.cost_service.dto.response.WithholdingTaxRuleResponse;
import com.example.cost_service.mapper.WithholdingTaxRuleMapper;
import com.example.cost_service.model.WithholdingTaxRule;
import com.example.cost_service.repository.WithholdingTaxRuleRepository;
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
public class WithholdingTaxRuleService {

    private final WithholdingTaxRuleRepository repository;
    private final WithholdingTaxRuleMapper mapper;
    private final ValidationUtil validationUtil;
    private final SecurityUtil securityUtil;

    @Transactional
    public WithholdingTaxRuleResponse addWithholdingTaxRule(UUID tenantId, WithholdingTaxRuleRequest request) {
        WithholdingTaxRule withholdingTaxRule = mapper.mapToEntity(request, tenantId);
        
        // Set audit fields from JWT
        UUID userId = securityUtil.getUserId();
        String username = securityUtil.getUsername();
        if (userId != null) {
            withholdingTaxRule.setCreatedBy(userId.toString());
            withholdingTaxRule.setUpdatedBy(userId.toString());
            withholdingTaxRule.setCreatedByUsername(username);
            withholdingTaxRule.setUpdatedByUsername(username);
        } else {
            log.warn("Unable to extract user ID from JWT for audit fields");
        }
        
        WithholdingTaxRule savedWithholdingTaxRule = repository.save(withholdingTaxRule);
        log.info("Withholding tax rule created with id: {}", savedWithholdingTaxRule.getId());
        return mapper.mapToDto(savedWithholdingTaxRule);
    }

    public List<WithholdingTaxRuleResponse> getAllWithholdingTaxRules(UUID tenantId) {
        return repository.findByTenantId(tenantId)
                .stream()
                .map(mapper::mapToDto)
                .toList();
    }

    public WithholdingTaxRuleResponse getWithholdingTaxRuleById(UUID tenantId, UUID withholdingTaxRuleId) {
        WithholdingTaxRule withholdingTaxRule = validationUtil.getWithholdingTaxRuleById(tenantId, withholdingTaxRuleId);
        return mapper.mapToDto(withholdingTaxRule);
    }

    @Transactional
    public WithholdingTaxRuleResponse updateWithholdingTaxRule(UUID tenantId, UUID withholdingTaxRuleId, WithholdingTaxRuleRequest request) {
        WithholdingTaxRule withholdingTaxRule = validationUtil.getWithholdingTaxRuleById(tenantId, withholdingTaxRuleId);
        withholdingTaxRule = mapper.mapUpdateRequest(withholdingTaxRule, request);
        
        // Set updatedBy audit field from JWT
        UUID userId = securityUtil.getUserId();
        String username = securityUtil.getUsername();
        if (userId != null) {
            withholdingTaxRule.setUpdatedBy(userId.toString());
            withholdingTaxRule.setUpdatedByUsername(username);
        } else {
            log.warn("Unable to extract user ID from JWT for updatedBy audit field");
        }
        
        WithholdingTaxRule updatedWithholdingTaxRule = repository.save(withholdingTaxRule);
        log.info("Withholding tax rule updated with id: {}", updatedWithholdingTaxRule.getId());
        return mapper.mapToDto(updatedWithholdingTaxRule);
    }

    @Transactional
    public void deleteWithholdingTaxRule(UUID tenantId, UUID withholdingTaxRuleId) {
        WithholdingTaxRule withholdingTaxRule = validationUtil.getWithholdingTaxRuleById(tenantId, withholdingTaxRuleId);
        repository.delete(withholdingTaxRule);
        log.info("Withholding tax rule deleted with id: {}", withholdingTaxRuleId);
    }
}
