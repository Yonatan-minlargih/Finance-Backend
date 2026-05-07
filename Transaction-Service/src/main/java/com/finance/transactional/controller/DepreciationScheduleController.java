package com.finance.transactional.controller;

import com.finance.transactional.dto.DepreciationScheduleDto;
import com.finance.transactional.service.DepreciationScheduleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactional/assets/depreciations/{tenantId}")
@RequiredArgsConstructor
@Tag(name = "DepreciationSchedule API", description = "Endpoints for managing DepreciationSchedule")
public class DepreciationScheduleController {

    private final DepreciationScheduleService service;

    @PostMapping("/add")
    public ResponseEntity<?> addDepreciationSchedule(
            @PathVariable UUID tenantId,
            @Valid @RequestBody DepreciationScheduleDto dto) {

        DepreciationScheduleDto response = service.createDepreciationSchedule(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllDepreciationSchedules(
            @PathVariable UUID tenantId) {

        List<DepreciationScheduleDto> responses = service.getAllDepreciationSchedules(tenantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getDepreciationScheduleById(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        DepreciationScheduleDto response = service.getDepreciationScheduleById(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDepreciationSchedule(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody DepreciationScheduleDto dto) {

        DepreciationScheduleDto response = service.updateDepreciationSchedule(tenantId, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDepreciationSchedule(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {

        service.deleteDepreciationSchedule(tenantId, id);
        return ResponseEntity.status(HttpStatus.OK).body("DepreciationSchedule deleted successfully!");
    }
}
