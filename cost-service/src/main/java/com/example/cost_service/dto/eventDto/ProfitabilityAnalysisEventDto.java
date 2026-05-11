package com.example.cost_service.dto.eventDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfitabilityAnalysisEventDto {
    
    private UUID id;
    private UUID tenantId;
    private BigDecimal revenue;
    private BigDecimal cogs;
    private BigDecimal grossProfit;
    private LocalDate analysisDate;
    private UUID periodId;
    private UUID productId;
    private UUID costCenterId;
    private UUID profitCenterId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private String createdByUsername;
    private String updatedByUsername;
}
