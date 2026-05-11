package com.example.cost_service.dto.response;

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
public class StandardCostRateResponse {
    private UUID id;
    private UUID tenantId;
    private UUID productId;
    private String itemCode;
    private BigDecimal rate;
    private LocalDate effectiveDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
