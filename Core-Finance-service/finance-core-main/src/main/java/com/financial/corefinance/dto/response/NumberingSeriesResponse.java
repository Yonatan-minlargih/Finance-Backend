package com.financial.corefinance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumberingSeriesResponse {
    private UUID id;
    private String tenantId;
    private String seriesCode;
    private String seriesName;
    private String description;
    private String prefix;
    private String suffix;
    private Long currentNumber;
    private Long startNumber;
    private Long endNumber;
    private Integer numberLength;
    private String resetFrequency;
    private String separator;
    private String format;
    private Boolean isActive;
    private Boolean allowManualOverride;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
