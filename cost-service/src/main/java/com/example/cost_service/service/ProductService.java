package com.example.cost_service.service;

import com.example.cost_service.dto.request.ProductRequest;
import com.example.cost_service.dto.response.ProductResponse;
import com.example.cost_service.exception.DuplicateResourceException;
import com.example.cost_service.mapper.ProductMapper;
import com.example.cost_service.model.Product;
import com.example.cost_service.repository.ProductRepository;
import com.example.cost_service.utility.SecurityUtil;
import com.example.cost_service.utility.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final ValidationUtil validationUtil;
    private final SecurityUtil securityUtil;
    private final CostEventService eventService;

    @Transactional
    public ProductResponse addProduct(UUID tenantId, ProductRequest request) {
        if (repository.existsByTenantIdAndName(tenantId, request.getName())) {
            throw new DuplicateResourceException("Product already exists");
        }

        Product product = mapper.mapToEntity(request, tenantId);
        
        // Set audit fields from JWT
        UUID userId = securityUtil.getUserId();
        String username = securityUtil.getUsername();
        if (userId != null) {
            product.setCreatedBy(userId.toString());
            product.setUpdatedBy(userId.toString());
            product.setCreatedByUsername(username);
            product.setUpdatedByUsername(username);
        } else {
            log.warn("Unable to extract user ID from JWT for audit fields");
        }
        
        Product savedProduct = repository.save(product);
        log.info("Product created with id: {}", savedProduct.getId());
        log.info("🔍 STEP 1: Product entity saved successfully. Preparing to publish event. EntityId={}, TenantId={}", 
                savedProduct.getId(), savedProduct.getTenantId());
        
        // Publish create event
        eventService.publishProductCreated(savedProduct);
        
        return mapper.mapToDto(savedProduct);
    }

    public List<ProductResponse> getAllProducts(UUID tenantId) {
        return repository.findByTenantId(tenantId)
                .stream()
                .map(mapper::mapToDto)
                .toList();
    }

    public ProductResponse getProductById(UUID tenantId, UUID productId) {
        Product product = validationUtil.getProductById(tenantId, productId);
        return mapper.mapToDto(product);
    }

    @Transactional
    public ProductResponse updateProduct(UUID tenantId, UUID productId, ProductRequest request) {
        Product product = validationUtil.getProductById(tenantId, productId);
        
        if (repository.existsByTenantIdAndNameAndIdNot(tenantId, request.getName(), productId)) {
            throw new DuplicateResourceException("Product with this name already exists");
        }

        product = mapper.mapUpdateRequest(product, request);
        
        // Set updatedBy audit field from JWT
        UUID userId = securityUtil.getUserId();
        String username = securityUtil.getUsername();
        if (userId != null) {
            product.setUpdatedBy(userId.toString());
            product.setUpdatedByUsername(username);
        } else {
            log.warn("Unable to extract user ID from JWT for updatedBy audit field");
        }
        
        Product updatedProduct = repository.save(product);
        log.info("Product updated with id: {}", updatedProduct.getId());
        log.info("🔍 STEP 1: Product entity updated successfully. Preparing to publish event. EntityId={}, TenantId={}", 
                updatedProduct.getId(), updatedProduct.getTenantId());
        
        // Publish update event
        eventService.publishProductUpdated(updatedProduct);
        
        return mapper.mapToDto(updatedProduct);
    }

    @Transactional
    public void deleteProduct(UUID tenantId, UUID productId) {
        Product product = validationUtil.getProductById(tenantId, productId);
        log.info("🔍 STEP 1: Product entity retrieved for deletion. Preparing to publish event. EntityId={}, TenantId={}", 
                product.getId(), product.getTenantId());
        
        // Publish delete event before deletion
        eventService.publishProductDeleted(product);
        
        repository.delete(product);
        log.info("Product deleted with id: {}", productId);
    }
}
