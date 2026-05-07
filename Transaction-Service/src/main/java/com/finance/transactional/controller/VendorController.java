package com.finance.transactional.controller;

import com.finance.transactional.dto.VendorDto;
import com.finance.transactional.service.VendorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactional/ap/vendors/{tenantId}")
@RequiredArgsConstructor
@Tag(name = "Vendor API", description = "Endpoints for managing Vendor")
public class VendorController {

    private final VendorService service;

    @PostMapping("/add")
    public ResponseEntity<?> addVendor(
            @PathVariable UUID tenantId,
            @Valid @RequestBody VendorDto dto) {

        VendorDto response = service.createVendor(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllVendors(
            @PathVariable UUID tenantId) {

        List<VendorDto> responses = service.getAllVendors(tenantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getVendorById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        VendorDto response = service.getVendorById(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateVendor(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody VendorDto dto) {

        VendorDto response = service.updateVendor(tenantId, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteVendor(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        service.deleteVendor(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body("Vendor deleted successfully!");
    }
}
