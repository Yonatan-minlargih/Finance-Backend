package com.example.cost_service.dto.eventDto;

import com.example.cost_service.enums.ApplicableTo;
import com.example.cost_service.enums.TaxType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithholdingTaxRuleEventDto {
    
    private UUID id;
    private UUID tenantId;
    private String taxName;
    private TaxType taxType;
    private BigDecimal rate;
    private ApplicableTo applicableTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private String createdByUsername;
    private String updatedByUsername;
}
