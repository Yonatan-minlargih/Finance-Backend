package com.example.cost_service.controller;

import com.example.cost_service.dto.request.ProfitabilityAnalysisRequest;
import com.example.cost_service.dto.response.ProfitabilityAnalysisResponse;
import com.example.cost_service.service.ProfitabilityAnalysisService;
import com.example.cost_service.utility.PermissionEvaluator;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profitability-analysis/{tenant-id}")
@RequiredArgsConstructor
@Tag(name = "Profitability Analysis")
public class ProfitabilityAnalysisController {

    private final ProfitabilityAnalysisService profitabilityAnalysisService;
    private final PermissionEvaluator permissionEvaluator;

    @PostMapping("/add")
    public ResponseEntity<?> addProfitabilityAnalysis(
            @PathVariable("tenant-id") UUID tenantId,
            @RequestBody ProfitabilityAnalysisRequest request) {

        permissionEvaluator.addProfitabilityAnalysisPermission(tenantId);

        ProfitabilityAnalysisResponse response = profitabilityAnalysisService.addProfitabilityAnalysis(tenantId, request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllProfitabilityAnalyses(
            @PathVariable("tenant-id") UUID tenantId) {

        permissionEvaluator.getAllProfitabilityAnalysesPermission(tenantId);

        List<ProfitabilityAnalysisResponse> analyses = profitabilityAnalysisService.getAllProfitabilityAnalyses(tenantId);
        return ResponseEntity.ok(analyses);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getProfitabilityAnalysisById(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID profitabilityAnalysisId) {

        permissionEvaluator.getProfitabilityAnalysisByIdPermission(tenantId, profitabilityAnalysisId);

        return ResponseEntity.ok(profitabilityAnalysisService.getProfitabilityAnalysisById(tenantId, profitabilityAnalysisId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProfitabilityAnalysis(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID profitabilityAnalysisId,
            @RequestBody ProfitabilityAnalysisRequest request) {

        permissionEvaluator.updateProfitabilityAnalysisPermission(tenantId);

        return ResponseEntity.ok(profitabilityAnalysisService.updateProfitabilityAnalysis(tenantId, profitabilityAnalysisId, request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProfitabilityAnalysis(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID profitabilityAnalysisId) {

        permissionEvaluator.deleteProfitabilityAnalysisPermission(tenantId);

        profitabilityAnalysisService.deleteProfitabilityAnalysis(tenantId, profitabilityAnalysisId);
        return ResponseEntity.ok("Deleted successfully");
    }
}
