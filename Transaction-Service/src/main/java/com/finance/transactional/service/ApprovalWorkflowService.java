package com.finance.transactional.service;

import com.finance.transactional.dto.ApprovalWorkflowDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.system.ApprovalWorkflow;
import com.finance.transactional.mapper.ApprovalWorkflowMapper;
import com.finance.transactional.repository.ApprovalWorkflowRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApprovalWorkflowService {

    private final ApprovalWorkflowRepository repository;
    private final ApprovalWorkflowMapper mapper;

    @Transactional
    public ApprovalWorkflowDto createApprovalWorkflow(UUID tenantId, ApprovalWorkflowDto dto) {
        ApprovalWorkflow approvalWorkflow = mapper.toEntity(dto);
        approvalWorkflow.setTenantId(tenantId);
        ApprovalWorkflow saved = repository.save(approvalWorkflow);
        return mapper.toDto(saved);
    }

    @Transactional
    public ApprovalWorkflowDto updateApprovalWorkflow(UUID tenantId, UUID id, ApprovalWorkflowDto dto) {
        ApprovalWorkflow existing = getExistingApprovalWorkflow(tenantId, id);
        ApprovalWorkflow updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public ApprovalWorkflowDto getApprovalWorkflowById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingApprovalWorkflow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<ApprovalWorkflowDto> getAllApprovalWorkflows(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteApprovalWorkflow(UUID tenantId, UUID id) {
        ApprovalWorkflow approvalWorkflow = getExistingApprovalWorkflow(tenantId, id);
        repository.delete(approvalWorkflow);
    }

    private ApprovalWorkflow getExistingApprovalWorkflow(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalWorkflow not found with id " + id));
    }
}
