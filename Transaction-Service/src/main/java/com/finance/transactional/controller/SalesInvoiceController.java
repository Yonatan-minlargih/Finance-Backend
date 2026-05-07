package com.finance.transactional.controller;

import com.finance.transactional.dto.SalesInvoiceDto;
import com.finance.transactional.service.SalesInvoiceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactional/ar/sales-invoices/{tenantId}")
@RequiredArgsConstructor
@Tag(name = "SalesInvoice API", description = "Endpoints for managing SalesInvoice")
public class SalesInvoiceController {

    private final SalesInvoiceService service;

    @PostMapping("/add")
    public ResponseEntity<?> addSalesInvoice(
            @PathVariable UUID tenantId,
            @Valid @RequestBody SalesInvoiceDto dto) {

        SalesInvoiceDto response = service.createSalesInvoice(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllSalesInvoices(
            @PathVariable UUID tenantId) {

        List<SalesInvoiceDto> responses = service.getAllSalesInvoices(tenantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getSalesInvoiceById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        SalesInvoiceDto response = service.getSalesInvoiceById(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateSalesInvoice(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody SalesInvoiceDto dto) {

        SalesInvoiceDto response = service.updateSalesInvoice(tenantId, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteSalesInvoice(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        service.deleteSalesInvoice(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body("SalesInvoice deleted successfully!");
    }
}
