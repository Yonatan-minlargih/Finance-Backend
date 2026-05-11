package com.example.cost_service.dto.response;

import com.example.cost_service.enums.ActiveStatus;
import com.example.cost_service.enums.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private UUID id;
    private UUID tenantId;
    private String productCode;
    private String name;
    private ProductCategory category;
    private ActiveStatus isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
