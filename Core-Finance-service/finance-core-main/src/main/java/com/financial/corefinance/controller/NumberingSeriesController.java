package com.financial.corefinance.controller;

import com.financial.corefinance.domain.entity.NumberingSeries;
import com.financial.corefinance.dto.request.NumberingSeriesRequest;
import com.financial.corefinance.dto.response.NumberingSeriesResponse;
import com.financial.corefinance.mapper.NumberingSeriesMapper;
import com.financial.corefinance.service.NumberingSeriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/numbering-series")
@RequiredArgsConstructor
@Tag(name = "Numbering Series", description = "APIs for managing document numbering series")
public class NumberingSeriesController {

    private final NumberingSeriesService service;
    private final NumberingSeriesMapper mapper;

    @PostMapping
    @Operation(summary = "Create Numbering Series")
    public ResponseEntity<NumberingSeriesResponse> createSeries(@Valid @RequestBody NumberingSeriesRequest request) {
        NumberingSeries created = service.createSeries(mapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping
    @Operation(summary = "Get All Numbering Series")
    public ResponseEntity<List<NumberingSeriesResponse>> getAllSeries() {
        List<NumberingSeriesResponse> list = service.getAllSeries().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Numbering Series by ID")
    public ResponseEntity<NumberingSeriesResponse> getSeriesById(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(service.getSeriesById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Numbering Series")
    public ResponseEntity<NumberingSeriesResponse> updateSeries(@PathVariable UUID id, @Valid @RequestBody NumberingSeriesRequest request) {
        NumberingSeries existing = service.getSeriesById(id);
        mapper.updateEntity(existing, request);
        return ResponseEntity.ok(mapper.toResponse(service.updateSeries(id, existing)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Numbering Series")
    public ResponseEntity<Void> deleteSeries(@PathVariable UUID id) {
        service.deleteSeries(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Thread-safe endpoint for obtaining the next document number from a series.
     * Auto-creates the series if it doesn't exist.
     * Used by Transaction-Service (via Feign) for AP invoices, AR invoices, payments, receipts.
     */
    @PostMapping("/next/{seriesCode}")
    @Operation(summary = "Get Next Number from Series",
               description = "Atomically generates and returns the next document control number for the given series code. Auto-creates the series if not found.")
    public ResponseEntity<Map<String, String>> getNextNumber(@PathVariable String seriesCode) {
        return ResponseEntity.ok(service.getNextNumber(seriesCode));
    }
}
