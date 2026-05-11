package com.example.cost_service.controller;

import com.example.cost_service.dto.request.ProductRequest;
import com.example.cost_service.dto.response.ProductResponse;
import com.example.cost_service.service.ProductService;
import com.example.cost_service.utility.PermissionEvaluator;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product/{tenant-id}")
@RequiredArgsConstructor
@Tag(name = "Product")
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final PermissionEvaluator permissionEvaluator;

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(
            @PathVariable("tenant-id") UUID tenantId,
            @RequestBody ProductRequest request) {

        log.info("🔍 STEP 0: API called - Creating product. TenantId={}, ProductName={}", tenantId, request.getName());

        permissionEvaluator.addProductPermission(tenantId);

        ProductResponse response = productService.addProduct(tenantId, request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllProducts(
            @PathVariable("tenant-id") UUID tenantId) {

        permissionEvaluator.getAllProductsPermission(tenantId);

        List<ProductResponse> products = productService.getAllProducts(tenantId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getProductById(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID productId) {

        permissionEvaluator.getProductByIdPermission(tenantId, productId);

        return ResponseEntity.ok(productService.getProductById(tenantId, productId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID productId,
            @RequestBody ProductRequest request) {

        permissionEvaluator.updateProductPermission(tenantId);

        return ResponseEntity.ok(productService.updateProduct(tenantId, productId, request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID productId) {

        permissionEvaluator.deleteProductPermission(tenantId);

        productService.deleteProduct(tenantId, productId);
        return ResponseEntity.ok("Deleted successfully");
    }
}
