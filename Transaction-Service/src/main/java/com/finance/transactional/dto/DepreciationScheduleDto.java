package com.finance.transactional.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class DepreciationScheduleDto {
    private UUID id;
    private UUID tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private UUID assetId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal depreciationAmount;
    private Boolean isPosted;
}
