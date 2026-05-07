package com.finance.transactional.controller;

import com.finance.transactional.dto.BankAccountDto;
import com.finance.transactional.service.BankAccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactional/banking/accounts/{tenantId}")
@RequiredArgsConstructor
@Tag(name = "BankAccount API", description = "Endpoints for managing BankAccount")
public class BankAccountController {

    private final BankAccountService service;

    @PostMapping("/add")
    public ResponseEntity<?> addBankAccount(
            @PathVariable UUID tenantId,
            @Valid @RequestBody BankAccountDto dto) {

        BankAccountDto response = service.createBankAccount(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllBankAccounts(
            @PathVariable UUID tenantId) {

        List<BankAccountDto> responses = service.getAllBankAccounts(tenantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getBankAccountById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        BankAccountDto response = service.getBankAccountById(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateBankAccount(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody BankAccountDto dto) {

        BankAccountDto response = service.updateBankAccount(tenantId, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteBankAccount(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        service.deleteBankAccount(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body("BankAccount deleted successfully!");
    }
}
