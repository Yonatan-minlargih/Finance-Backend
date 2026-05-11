package com.example.cost_service.controller;

import com.example.cost_service.dto.request.CostRecordRequest;
import com.example.cost_service.dto.response.CostRecordResponse;
import com.example.cost_service.service.CostRecordService;
import com.example.cost_service.utility.PermissionEvaluator;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cost-record/{tenant-id}")
@RequiredArgsConstructor
@Tag(name = "Cost Record")
public class CostRecordController {

    private final CostRecordService costRecordService;
    private final PermissionEvaluator permissionEvaluator;

    @PostMapping("/add")
    public ResponseEntity<?> addCostRecord(
            @PathVariable("tenant-id") UUID tenantId,
            @RequestBody CostRecordRequest request) {

        permissionEvaluator.addCostRecordPermission(tenantId);

        CostRecordResponse response = costRecordService.addCostRecord(tenantId, request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllCostRecords(
            @PathVariable("tenant-id") UUID tenantId) {

        permissionEvaluator.getAllCostRecordsPermission(tenantId);

        List<CostRecordResponse> costRecords = costRecordService.getAllCostRecords(tenantId);
        return ResponseEntity.ok(costRecords);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getCostRecordById(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID costRecordId) {

        permissionEvaluator.getCostRecordByIdPermission(tenantId, costRecordId);

        return ResponseEntity.ok(costRecordService.getCostRecordById(tenantId, costRecordId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCostRecord(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID costRecordId,
            @RequestBody CostRecordRequest request) {

        permissionEvaluator.updateCostRecordPermission(tenantId);

        return ResponseEntity.ok(costRecordService.updateCostRecord(tenantId, costRecordId, request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCostRecord(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID costRecordId) {

        permissionEvaluator.deleteCostRecordPermission(tenantId);

        costRecordService.deleteCostRecord(tenantId, costRecordId);
        return ResponseEntity.ok("Deleted successfully");
    }
}
