package com.example.cost_service.controller;

import com.example.cost_service.dto.request.ProfitCenterRequest;
import com.example.cost_service.dto.response.ProfitCenterResponse;
import com.example.cost_service.service.ProfitCenterService;
import com.example.cost_service.utility.PermissionEvaluator;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profit-center/{tenant-id}")
@RequiredArgsConstructor
@Tag(name = "Profit Center")
public class ProfitCenterController {

    private final ProfitCenterService profitCenterService;
    private final PermissionEvaluator permissionEvaluator;

    @PostMapping("/add")
    public ResponseEntity<?> addProfitCenter(
            @PathVariable("tenant-id") UUID tenantId,
            @RequestBody ProfitCenterRequest request) {

        permissionEvaluator.addProfitCenterPermission(tenantId);

        ProfitCenterResponse response = profitCenterService.addProfitCenter(tenantId, request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllProfitCenters(
            @PathVariable("tenant-id") UUID tenantId) {

        permissionEvaluator.getAllProfitCentersPermission(tenantId);

        List<ProfitCenterResponse> profitCenters = profitCenterService.getAllProfitCenters(tenantId);
        return ResponseEntity.ok(profitCenters);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getProfitCenterById(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID profitCenterId) {

        permissionEvaluator.getProfitCenterByIdPermission(tenantId, profitCenterId);

        return ResponseEntity.ok(profitCenterService.getProfitCenterById(tenantId, profitCenterId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProfitCenter(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID profitCenterId,
            @RequestBody ProfitCenterRequest request) {

        permissionEvaluator.updateProfitCenterPermission(tenantId);

        return ResponseEntity.ok(profitCenterService.updateProfitCenter(tenantId, profitCenterId, request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProfitCenter(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID profitCenterId) {

        permissionEvaluator.deleteProfitCenterPermission(tenantId);

        profitCenterService.deleteProfitCenter(tenantId, profitCenterId);
        return ResponseEntity.ok("Deleted successfully");
    }
}
