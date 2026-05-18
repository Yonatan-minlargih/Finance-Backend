package com.financial.corefinance.dto.eventDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollApprovedEventDto {
    private UUID id;
    private UUID tenantId;
    private String eventType;
    private LocalDate runDate;
    private UUID periodId;
    private BigDecimal totalGross;
    private BigDecimal totalNet;
    private String status;
}
