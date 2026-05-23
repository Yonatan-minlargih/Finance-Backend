package com.finance.transactional.controller;

import com.finance.transactional.dto.FixedAssetDto;
import com.finance.transactional.service.FixedAssetService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

    @PostMapping("/impair/{id}")
    public ResponseEntity<Map<String, Object>> impairFixedAsset(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "Asset impairment recorded") String description) {

        return ResponseEntity.status(HttpStatus.OK).body(service.impairAsset(tenantId, id, amount, description));
    }

    @PostMapping("/dispose/{id}")
    public ResponseEntity<Map<String, Object>> disposeFixedAsset(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "Asset disposal recorded") String description) {

        return ResponseEntity.status(HttpStatus.OK).body(service.disposeAsset(tenantId, id, description));
    }

    @PostMapping("/reclassify/{id}")
    public ResponseEntity<Map<String, Object>> reclassifyFixedAsset(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @RequestParam String assetCategory,
            @RequestParam(required = false) String costCenterCode,
            @RequestParam(required = false, defaultValue = "Asset reclassification recorded") String description,
            @RequestParam(required = false) String effectiveDate) {

        return ResponseEntity.status(HttpStatus.OK).body(service.reclassifyAsset(
                tenantId,
                id,
                assetCategory,
                costCenterCode,
                description,
                effectiveDate == null || effectiveDate.isBlank() ? null : LocalDate.parse(effectiveDate)));
    }
}
