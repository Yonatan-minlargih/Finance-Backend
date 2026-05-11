package com.example.cost_service.controller;

import com.example.cost_service.dto.request.StandardCostRateRequest;
import com.example.cost_service.dto.response.StandardCostRateResponse;
import com.example.cost_service.service.StandardCostRateService;
import com.example.cost_service.utility.PermissionEvaluator;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/standard-cost-rate/{tenant-id}")
@RequiredArgsConstructor
@Tag(name = "Standard Cost Rate")
public class StandardCostRateController {

    private final StandardCostRateService standardCostRateService;
    private final PermissionEvaluator permissionEvaluator;

    @PostMapping("/add")
    public ResponseEntity<?> addStandardCostRate(
            @PathVariable("tenant-id") UUID tenantId,
            @RequestBody StandardCostRateRequest request) {

        permissionEvaluator.addStandardCostRatePermission(tenantId);

        StandardCostRateResponse response = standardCostRateService.addStandardCostRate(tenantId, request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllStandardCostRates(
            @PathVariable("tenant-id") UUID tenantId) {

        permissionEvaluator.getAllStandardCostRatesPermission(tenantId);

        List<StandardCostRateResponse> standardCostRates = standardCostRateService.getAllStandardCostRates(tenantId);
        return ResponseEntity.ok(standardCostRates);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getStandardCostRateById(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID standardCostRateId) {

        permissionEvaluator.getStandardCostRateByIdPermission(tenantId, standardCostRateId);

        return ResponseEntity.ok(standardCostRateService.getStandardCostRateById(tenantId, standardCostRateId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateStandardCostRate(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID standardCostRateId,
            @RequestBody StandardCostRateRequest request) {

        permissionEvaluator.updateStandardCostRatePermission(tenantId);

        return ResponseEntity.ok(standardCostRateService.updateStandardCostRate(tenantId, standardCostRateId, request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteStandardCostRate(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID standardCostRateId) {

        permissionEvaluator.deleteStandardCostRatePermission(tenantId);

        standardCostRateService.deleteStandardCostRate(tenantId, standardCostRateId);
        return ResponseEntity.ok("Deleted successfully");
    }
}
