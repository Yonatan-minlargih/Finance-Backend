package com.example.cost_service.dto.eventDto;

import com.example.cost_service.enums.ActiveStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfitCenterEventDto {
    
    private UUID id;
    private UUID tenantId;
    private String code;
    private String name;
    private ActiveStatus isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private String createdByUsername;
    private String updatedByUsername;
}
