package com.finance.transactional.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class AuditLogDto {
    private UUID id;
    private UUID tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private String entityName;
    private String entityId;
    private String action;
    private String oldValue;
    private String newValue;
}
