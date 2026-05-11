package com.example.cost_service.mapper;

import com.example.cost_service.dto.request.ProductRequest;
import com.example.cost_service.dto.response.ProductResponse;
import com.example.cost_service.model.Product;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductMapper {

    public Product mapToEntity(ProductRequest request, UUID tenantId) {
        Product product = new Product();
        product.setTenantId(tenantId);
        product.setProductCode(request.getProductCode());
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setIsActive(request.getIsActive());
        return product;
    }

    public ProductResponse mapToDto(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setTenantId(product.getTenantId());
        response.setProductCode(product.getProductCode());
        response.setName(product.getName());
        response.setCategory(product.getCategory());
        response.setIsActive(product.getIsActive());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        response.setCreatedBy(product.getCreatedBy());
        response.setUpdatedBy(product.getUpdatedBy());
        return response;
    }

    public Product mapUpdateRequest(Product product, ProductRequest request) {
        product.setProductCode(request.getProductCode());
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setIsActive(request.getIsActive());
        return product;
    }
}
