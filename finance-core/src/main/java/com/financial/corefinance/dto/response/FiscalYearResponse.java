package com.financial.corefinance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class FiscalYearResponse {
    private UUID id;
    private String tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
    private Integer yearNumber;
    private String yearName;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID calendarDefinitionId;
    private Boolean isCurrent;
    private Boolean isClosed;
    private LocalDate closedAt;
    private String closedBy;
    private Integer totalPeriods;
    private String description;
}