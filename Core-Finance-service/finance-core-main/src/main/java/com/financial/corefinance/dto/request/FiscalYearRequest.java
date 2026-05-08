package com.financial.corefinance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class FiscalYearRequest {
    @NotBlank
    private String tenantId;
    @NotNull
    private Integer yearNumber;
    @NotBlank
    private String yearName;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @NotNull
    private UUID calendarDefinitionId;
    private Boolean isCurrent;
    private String description;
}