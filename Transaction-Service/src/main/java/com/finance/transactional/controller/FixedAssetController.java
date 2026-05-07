package com.finance.transactional.controller;

import com.finance.transactional.dto.FixedAssetDto;
import com.finance.transactional.service.FixedAssetService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactional/assets/fixed/{tenantId}")
@RequiredArgsConstructor
@Tag(name = "FixedAsset API", description = "Endpoints for managing FixedAsset")
public class FixedAssetController {

    private final FixedAssetService service;

    @PostMapping("/add")
    public ResponseEntity<?> addFixedAsset(
            @PathVariable UUID tenantId,
            @Valid @RequestBody FixedAssetDto dto) {

        FixedAssetDto response = service.createFixedAsset(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllFixedAssets(
            @PathVariable UUID tenantId) {

        List<FixedAssetDto> responses = service.getAllFixedAssets(tenantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getFixedAssetById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        FixedAssetDto response = service.getFixedAssetById(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateFixedAsset(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody FixedAssetDto dto) {

        FixedAssetDto response = service.updateFixedAsset(tenantId, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteFixedAsset(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        service.deleteFixedAsset(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body("FixedAsset deleted successfully!");
    }
}
