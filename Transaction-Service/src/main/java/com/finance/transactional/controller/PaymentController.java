package com.finance.transactional.controller;

import com.finance.transactional.dto.PaymentDto;
import com.finance.transactional.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactional/ap/payments/{tenantId}")
@RequiredArgsConstructor
@Tag(name = "Payment API", description = "Endpoints for managing Payment")
public class PaymentController {

    private final PaymentService service;

    @PostMapping("/add")
    public ResponseEntity<?> addPayment(
            @PathVariable UUID tenantId,
            @Valid @RequestBody PaymentDto dto) {

        PaymentDto response = service.createPayment(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllPayments(
            @PathVariable UUID tenantId) {

        List<PaymentDto> responses = service.getAllPayments(tenantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getPaymentById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        PaymentDto response = service.getPaymentById(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePayment(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody PaymentDto dto) {

        PaymentDto response = service.updatePayment(tenantId, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePayment(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        service.deletePayment(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body("Payment deleted successfully!");
    }
}
