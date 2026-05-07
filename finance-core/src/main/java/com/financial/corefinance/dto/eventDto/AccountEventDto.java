package com.financial.corefinance.dto.eventDto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountEventDto {
    private String eventType;
    private String eventVersion;
    private String eventId;
    private String correlationId;
    private String tenantId;
    private UUID accountId;
    private String accountCode;
    private String accountName;
    private String triggeredBy;
    private LocalDateTime occurredAt;
}
