package com.financial.corefinance.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CalendarDefinitionRequest {
    @NotBlank
    private String tenantId;
    @NotBlank
    private String calendarName;
    private String description;
    private Boolean isDefault;
    private String periodType; // MONTHLY, QUARTERLY
    private Integer yearStartMonth;
    private Integer yearStartDay;
    private LocalDate effectiveFrom;
}
