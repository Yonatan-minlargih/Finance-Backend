package com.finance.transactional.controller;

import com.finance.transactional.dto.PhysicalInventoryCountDto;
import com.finance.transactional.service.PhysicalInventoryCountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactional/assets/inventory-counts/{tenantId}")
@RequiredArgsConstructor
@Tag(name = "PhysicalInventoryCount API", description = "Endpoints for managing PhysicalInventoryCount")
public class PhysicalInventoryCountController {

    private final PhysicalInventoryCountService service;

    @PostMapping("/add")
    public ResponseEntity<?> addPhysicalInventoryCount(
            @PathVariable UUID tenantId,
            @Valid @RequestBody PhysicalInventoryCountDto dto) {

        PhysicalInventoryCountDto response = service.createPhysicalInventoryCount(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllPhysicalInventoryCounts(
            @PathVariable UUID tenantId) {

        List<PhysicalInventoryCountDto> responses = service.getAllPhysicalInventoryCounts(tenantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getPhysicalInventoryCountById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        PhysicalInventoryCountDto response = service.getPhysicalInventoryCountById(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePhysicalInventoryCount(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody PhysicalInventoryCountDto dto) {

        PhysicalInventoryCountDto response = service.updatePhysicalInventoryCount(tenantId, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePhysicalInventoryCount(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        service.deletePhysicalInventoryCount(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body("PhysicalInventoryCount deleted successfully!");
    }
}
