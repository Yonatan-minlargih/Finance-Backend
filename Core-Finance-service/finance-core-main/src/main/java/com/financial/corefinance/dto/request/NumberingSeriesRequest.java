package com.financial.corefinance.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumberingSeriesRequest {
    @NotBlank(message = "Series code is required")
    private String seriesCode;
    @NotBlank(message = "Series name is required")
    private String seriesName;
    private String description;
    private String prefix;
    private String suffix;
    private Long currentNumber = 1L;
    private Long startNumber = 1L;
    private Long endNumber;
    private Integer numberLength = 6;
    private String resetFrequency;
    private String separator = "-";
    private Boolean isActive = true;
    private Boolean allowManualOverride = false;
}
