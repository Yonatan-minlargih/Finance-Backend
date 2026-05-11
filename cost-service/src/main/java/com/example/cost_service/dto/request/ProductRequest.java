package com.example.cost_service.dto.request;

import com.example.cost_service.enums.ActiveStatus;
import com.example.cost_service.enums.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    
    @NotBlank(message = "Product code is required")
    private String productCode;
    
    @NotBlank(message = "Product name is required")
    private String name;
    
    @NotNull(message = "Product category is required")
    private ProductCategory category;
    
    @NotNull(message = "Active status is required")
    private ActiveStatus isActive;
}
