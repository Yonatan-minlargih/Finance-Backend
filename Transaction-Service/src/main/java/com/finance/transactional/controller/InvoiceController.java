package com.finance.transactional.controller;

import com.finance.transactional.dto.InvoiceDto;
import com.finance.transactional.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactional/ap/invoices/{tenantId}")
@RequiredArgsConstructor
@Tag(name = "Accounts Payable - Invoices", description = "Endpoints for managing AP Invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/add")
    public ResponseEntity<InvoiceDto> createInvoice(
            @PathVariable UUID tenantId,
            @Valid @RequestBody InvoiceDto invoiceDto) {

        InvoiceDto created = invoiceService.createInvoice(tenantId, invoiceDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<InvoiceDto> getInvoiceById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        return ResponseEntity.ok(invoiceService.getInvoiceById(tenantId, id));
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<InvoiceDto>> getAllInvoices(
            @PathVariable UUID tenantId) {

        return ResponseEntity.ok(invoiceService.getAllInvoices(tenantId));
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<InvoiceDto> approveInvoice(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        InvoiceDto approved = invoiceService.approveInvoice(tenantId, id);
        return ResponseEntity.ok(approved);
    }
}
