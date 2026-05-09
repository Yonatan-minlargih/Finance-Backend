package com.financial.corefinance.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class CalendarDefinitionResponse {
    private UUID id;
    private String tenantId;
    private String calendarName;
    private String description;
    private Boolean isDefault;
    private String periodType;
    private Integer yearStartMonth;
    private Integer yearStartDay;
    private LocalDate effectiveFrom;
}
