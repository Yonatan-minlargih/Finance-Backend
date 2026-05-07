package com.finance.transactional.mapper;

import com.finance.transactional.model.system.AuditLog;
import com.finance.transactional.dto.AuditLogDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuditLogMapper {
    AuditLogDto toDto(AuditLog entity);
    AuditLog toEntity(AuditLogDto dto);
}
