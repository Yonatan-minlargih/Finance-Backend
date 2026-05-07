package com.finance.transactional.controller;

import com.finance.transactional.dto.AssetTransactionDto;
import com.finance.transactional.service.AssetTransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactional/assets/transactions/{tenantId}")
@RequiredArgsConstructor
@Tag(name = "AssetTransaction API", description = "Endpoints for managing AssetTransaction")
public class AssetTransactionController {

    private final AssetTransactionService service;

    @PostMapping("/add")
    public ResponseEntity<?> addAssetTransaction(
            @PathVariable UUID tenantId,
            @Valid @RequestBody AssetTransactionDto dto) {

        AssetTransactionDto response = service.createAssetTransaction(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllAssetTransactions(
            @PathVariable UUID tenantId) {

        List<AssetTransactionDto> responses = service.getAllAssetTransactions(tenantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getAssetTransactionById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        AssetTransactionDto response = service.getAssetTransactionById(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateAssetTransaction(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody AssetTransactionDto dto) {

        AssetTransactionDto response = service.updateAssetTransaction(tenantId, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAssetTransaction(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        service.deleteAssetTransaction(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body("AssetTransaction deleted successfully!");
    }
}
