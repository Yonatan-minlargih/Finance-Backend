package com.example.cost_service.controller;

import com.example.cost_service.dto.request.CostCenterRequest;
import com.example.cost_service.dto.response.CostCenterResponse;
import com.example.cost_service.service.CostCenterService;
import com.example.cost_service.utility.PermissionEvaluator;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cost-center/{tenant-id}")
@RequiredArgsConstructor
@Tag(name = "Cost Center")
public class CostCenterController {

    private final CostCenterService costCenterService;
    private final PermissionEvaluator permissionEvaluator;

    @PostMapping("/add")
    public ResponseEntity<?> addCostCenter(
            @PathVariable("tenant-id") UUID tenantId,
            @RequestBody CostCenterRequest request) {

        permissionEvaluator.addCostCenterPermission(tenantId);

        CostCenterResponse response = costCenterService.addCostCenter(tenantId, request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllCostCenters(
            @PathVariable("tenant-id") UUID tenantId) {

        permissionEvaluator.getAllCostCentersPermission(tenantId);

        List<CostCenterResponse> costCenters = costCenterService.getAllCostCenters(tenantId);
        return ResponseEntity.ok(costCenters);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getCostCenterById(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID costCenterId) {

        permissionEvaluator.getCostCenterByIdPermission(tenantId, costCenterId);

        return ResponseEntity.ok(costCenterService.getCostCenterById(tenantId, costCenterId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCostCenter(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID costCenterId,
            @RequestBody CostCenterRequest request) {

        permissionEvaluator.updateCostCenterPermission(tenantId);

        return ResponseEntity.ok(costCenterService.updateCostCenter(tenantId, costCenterId, request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCostCenter(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID costCenterId) {

        permissionEvaluator.deleteCostCenterPermission(tenantId);

        costCenterService.deleteCostCenter(tenantId, costCenterId);
        return ResponseEntity.ok("Deleted successfully");
    }
}
