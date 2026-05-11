package com.example.cost_service.dto.response;

import com.example.cost_service.enums.FormulaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CogsFormulaResponse {
    private UUID id;
    private UUID tenantId;
    private FormulaType formulaName;
    private String formulaJson;
    private UUID periodId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
