package com.finance.transactional.controller;

import com.finance.transactional.dto.BankTransactionDto;
import com.finance.transactional.service.BankTransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactional/banking/transactions/{tenantId}")
@RequiredArgsConstructor
@Tag(name = "BankTransaction API", description = "Endpoints for managing BankTransaction")
public class BankTransactionController {

    private final BankTransactionService service;

    @PostMapping("/add")
    public ResponseEntity<?> addBankTransaction(
            @PathVariable UUID tenantId,
            @Valid @RequestBody BankTransactionDto dto) {

        BankTransactionDto response = service.createBankTransaction(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllBankTransactions(
            @PathVariable UUID tenantId) {

        List<BankTransactionDto> responses = service.getAllBankTransactions(tenantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getBankTransactionById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        BankTransactionDto response = service.getBankTransactionById(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateBankTransaction(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody BankTransactionDto dto) {

        BankTransactionDto response = service.updateBankTransaction(tenantId, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteBankTransaction(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        service.deleteBankTransaction(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body("BankTransaction deleted successfully!");
    }
}
