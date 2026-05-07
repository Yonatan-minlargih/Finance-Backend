package com.finance.transactional.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class PhysicalInventoryCountDto {
    private UUID id;
    private UUID tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private UUID assetId;
    private LocalDate countDate;
    private String countedBy;
    private Boolean isFound;
    private String conditionNotes;
}
