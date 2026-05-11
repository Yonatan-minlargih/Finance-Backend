package com.example.cost_service.dto.request;

import com.example.cost_service.enums.FormulaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CogsFormulaRequest {
    
    @NotNull(message = "Formula type is required")
    private FormulaType formulaName;
    
    @NotBlank(message = "Formula JSON is required")
    private String formulaJson;
    
    @NotNull(message = "Period ID is required")
    private UUID periodId;
}
