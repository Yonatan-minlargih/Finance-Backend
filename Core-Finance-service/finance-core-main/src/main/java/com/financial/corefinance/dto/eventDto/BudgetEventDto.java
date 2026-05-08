package com.financial.corefinance.dto.eventDto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BudgetEventDto {
    private String eventType;
    private String eventVersion;
    private String eventId;
    private String correlationId;
    private String tenantId;
    private UUID budgetId;
    private UUID fiscalYearId;
    private String budgetName;
    private String status;
    private String triggeredBy;
    private LocalDateTime occurredAt;
}