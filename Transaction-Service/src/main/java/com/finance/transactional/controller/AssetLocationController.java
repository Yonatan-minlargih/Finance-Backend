package com.finance.transactional.controller;

import com.finance.transactional.dto.AssetLocationDto;
import com.finance.transactional.service.AssetLocationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactional/assets/locations/{tenantId}")
@RequiredArgsConstructor
@Tag(name = "AssetLocation API", description = "Endpoints for managing AssetLocation")
public class AssetLocationController {

    private final AssetLocationService service;

    @PostMapping("/add")
    public ResponseEntity<?> addAssetLocation(
            @PathVariable UUID tenantId,
            @Valid @RequestBody AssetLocationDto dto) {

        AssetLocationDto response = service.createAssetLocation(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllAssetLocations(
            @PathVariable UUID tenantId) {

        List<AssetLocationDto> responses = service.getAllAssetLocations(tenantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getAssetLocationById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        AssetLocationDto response = service.getAssetLocationById(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateAssetLocation(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody AssetLocationDto dto) {

        AssetLocationDto response = service.updateAssetLocation(tenantId, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAssetLocation(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        service.deleteAssetLocation(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body("AssetLocation deleted successfully!");
    }
}
