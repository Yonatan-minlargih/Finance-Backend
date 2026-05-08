package com.financial.corefinance.dto.eventDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PeriodEventDto {
    private String eventType;
    private String eventVersion;
    private String eventId;
    private String correlationId;
    private String tenantId;
    private UUID fiscalYearId;
    private UUID accountingPeriodId;
    private Integer periodNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String triggeredBy;
    private LocalDateTime occurredAt;
}