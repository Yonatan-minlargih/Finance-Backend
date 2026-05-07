package com.finance.transactional.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class AssetLocationDto {
    private UUID id;
    private UUID tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private UUID assetId;
    private String locationName;
    private String departmentCode;
    private String custodianName;
    private Boolean isCurrent;
}
