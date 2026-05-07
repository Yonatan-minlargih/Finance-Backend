package com.finance.transactional.controller;

import com.finance.transactional.dto.ReceiptDto;
import com.finance.transactional.service.ReceiptService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactional/ar/receipts/{tenantId}")
@RequiredArgsConstructor
@Tag(name = "Receipt API", description = "Endpoints for managing Receipt")
public class ReceiptController {

    private final ReceiptService service;

    @PostMapping("/add")
    public ResponseEntity<?> addReceipt(
            @PathVariable UUID tenantId,
            @Valid @RequestBody ReceiptDto dto) {

        ReceiptDto response = service.createReceipt(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllReceipts(
            @PathVariable UUID tenantId) {

        List<ReceiptDto> responses = service.getAllReceipts(tenantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getReceiptById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        ReceiptDto response = service.getReceiptById(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateReceipt(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody ReceiptDto dto) {

        ReceiptDto response = service.updateReceipt(tenantId, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteReceipt(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        service.deleteReceipt(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body("Receipt deleted successfully!");
    }
}
