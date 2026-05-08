package com.financial.corefinance.dto.eventDto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinancialReportEventDto {
    private String eventType;
    private String eventVersion;
    private String eventId;
    private String correlationId;
    private String tenantId;
    private UUID reportId;
    private UUID fiscalYearId;
    private String reportType;
    private String status;
    private String generatedBy;
    private LocalDateTime occurredAt;
}