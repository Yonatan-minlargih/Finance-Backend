package com.finance.transactional.controller;

import com.finance.transactional.dto.PurchaseOrderDto;
import com.finance.transactional.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactional/ap/purchase-orders/{tenantId}")
@RequiredArgsConstructor
@Tag(name = "PurchaseOrder API", description = "Endpoints for managing PurchaseOrder")
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    @PostMapping("/add")
    public ResponseEntity<?> addPurchaseOrder(
            @PathVariable UUID tenantId,
            @Valid @RequestBody PurchaseOrderDto dto) {

        PurchaseOrderDto response = service.createPurchaseOrder(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllPurchaseOrders(
            @PathVariable UUID tenantId) {

        List<PurchaseOrderDto> responses = service.getAllPurchaseOrders(tenantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getPurchaseOrderById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        PurchaseOrderDto response = service.getPurchaseOrderById(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePurchaseOrder(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseOrderDto dto) {

        PurchaseOrderDto response = service.updatePurchaseOrder(tenantId, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePurchaseOrder(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        service.deletePurchaseOrder(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body("PurchaseOrder deleted successfully!");
    }
}
