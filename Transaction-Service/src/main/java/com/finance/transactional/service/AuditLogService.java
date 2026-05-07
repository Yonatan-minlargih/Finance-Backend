package com.finance.transactional.service;

import com.finance.transactional.dto.AuditLogDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.system.AuditLog;
import com.finance.transactional.mapper.AuditLogMapper;
import com.finance.transactional.repository.AuditLogRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;
    private final AuditLogMapper mapper;

    @Transactional
    public AuditLogDto createAuditLog(UUID tenantId, AuditLogDto dto) {
        AuditLog auditLog = mapper.toEntity(dto);
        auditLog.setTenantId(tenantId);
        AuditLog saved = repository.save(auditLog);
        return mapper.toDto(saved);
    }

    @Transactional
    public AuditLogDto updateAuditLog(UUID tenantId, UUID id, AuditLogDto dto) {
        AuditLog existing = getExistingAuditLog(tenantId, id);
        AuditLog updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public AuditLogDto getAuditLogById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingAuditLog(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<AuditLogDto> getAllAuditLogs(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteAuditLog(UUID tenantId, UUID id) {
        AuditLog auditLog = getExistingAuditLog(tenantId, id);
        repository.delete(auditLog);
    }

    private AuditLog getExistingAuditLog(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog not found with id " + id));
    }
}
