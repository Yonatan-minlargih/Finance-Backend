package com.financial.corefinance.dto.eventDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CostCalculatedEventDto {
    
    private UUID tenantId;
    private UUID productId;
    private UUID costCenterId;
    private UUID periodId;
    private BigDecimal materialCost;
    private BigDecimal laborCost;
    private BigDecimal overheadCost;
    private BigDecimal totalCost;
    private LocalDateTime calculatedAt;
    private String calculatedBy;
    private String calculationType;
}
