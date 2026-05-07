package com.financial.corefinance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class AccountingPeriodResponse {
    private UUID id;
    private String tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
    private UUID fiscalYearId;
    private Integer periodNumber;
    private String periodName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isOpen;
    private Boolean isClosed;
    private LocalDate closedAt;
    private String closedBy;
    private Boolean isAdjustmentPeriod;
    private String description;
}