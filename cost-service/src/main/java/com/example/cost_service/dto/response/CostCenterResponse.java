package com.example.cost_service.dto.response;

import com.example.cost_service.enums.ActiveStatus;
import com.example.cost_service.enums.CostCenterType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CostCenterResponse {
    private UUID id;
    private UUID tenantId;
    private String code;
    private String name;
    private CostCenterType type;
    private ActiveStatus isActive;
    private UUID profitCenterId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
