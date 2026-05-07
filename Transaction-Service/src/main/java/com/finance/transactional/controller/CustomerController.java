package com.finance.transactional.controller;

import com.finance.transactional.dto.CustomerDto;
import com.finance.transactional.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactional/ar/customers/{tenantId}")
@RequiredArgsConstructor
@Tag(name = "Customer API", description = "Endpoints for managing Customer")
public class CustomerController {

    private final CustomerService service;

    @PostMapping("/add")
    public ResponseEntity<?> addCustomer(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CustomerDto dto) {

        CustomerDto response = service.createCustomer(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllCustomers(
            @PathVariable UUID tenantId) {

        List<CustomerDto> responses = service.getAllCustomers(tenantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getCustomerById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        CustomerDto response = service.getCustomerById(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCustomer(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody CustomerDto dto) {

        CustomerDto response = service.updateCustomer(tenantId, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCustomer(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        service.deleteCustomer(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body("Customer deleted successfully!");
    }
}
