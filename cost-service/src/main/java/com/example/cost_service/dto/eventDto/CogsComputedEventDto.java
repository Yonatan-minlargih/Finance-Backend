package com.example.cost_service.dto.eventDto;

import com.example.cost_service.enums.FormulaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CogsComputedEventDto {
    
    private UUID tenantId;
    private UUID productId;
    private UUID periodId;
    private FormulaType formulaType;
    private BigDecimal totalCogs;
    private BigDecimal materialCost;
    private BigDecimal laborCost;
    private BigDecimal overheadCost;
    private LocalDateTime computedAt;
    private String computedBy;
    private String formulaDescription;
}
