package com.finance.transactional.controller;

import com.finance.transactional.dto.BankReconciliationDto;
import com.finance.transactional.service.BankReconciliationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactional/banking/reconciliations/{tenantId}")
@RequiredArgsConstructor
@Tag(name = "BankReconciliation API", description = "Endpoints for managing BankReconciliation")
public class BankReconciliationController {

    private final BankReconciliationService service;

    @PostMapping("/add")
    public ResponseEntity<?> addBankReconciliation(
            @PathVariable UUID tenantId,
            @Valid @RequestBody BankReconciliationDto dto) {

        BankReconciliationDto response = service.createBankReconciliation(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllBankReconciliations(
            @PathVariable UUID tenantId) {

        List<BankReconciliationDto> responses = service.getAllBankReconciliations(tenantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getBankReconciliationById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        BankReconciliationDto response = service.getBankReconciliationById(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateBankReconciliation(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody BankReconciliationDto dto) {

        BankReconciliationDto response = service.updateBankReconciliation(tenantId, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteBankReconciliation(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        service.deleteBankReconciliation(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body("BankReconciliation deleted successfully!");
    }
}
