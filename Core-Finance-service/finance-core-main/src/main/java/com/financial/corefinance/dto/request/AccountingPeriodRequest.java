package com.financial.corefinance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class AccountingPeriodRequest {
    @NotBlank
    private String tenantId;
    @NotNull
    private UUID fiscalYearId;
    @NotNull
    private Integer periodNumber;
    @NotBlank
    private String periodName;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    private Boolean isOpen;
    private Boolean isAdjustmentPeriod;
    private String description;
}