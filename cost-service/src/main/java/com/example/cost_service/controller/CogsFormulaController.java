package com.example.cost_service.controller;

import com.example.cost_service.dto.request.CogsFormulaRequest;
import com.example.cost_service.dto.response.CogsFormulaResponse;
import com.example.cost_service.service.CogsFormulaService;
import com.example.cost_service.utility.PermissionEvaluator;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cogs-formula/{tenant-id}")
@RequiredArgsConstructor
@Tag(name = "COGS Formula")
public class CogsFormulaController {

    private final CogsFormulaService cogsFormulaService;
    private final PermissionEvaluator permissionEvaluator;

    @PostMapping("/add")
    public ResponseEntity<?> addCogsFormula(
            @PathVariable("tenant-id") UUID tenantId,
            @RequestBody CogsFormulaRequest request) {

        permissionEvaluator.addCogsFormulaPermission(tenantId);

        CogsFormulaResponse response = cogsFormulaService.addCogsFormula(tenantId, request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllCogsFormulas(
            @PathVariable("tenant-id") UUID tenantId) {

        permissionEvaluator.getAllCogsFormulasPermission(tenantId);

        List<CogsFormulaResponse> cogsFormulas = cogsFormulaService.getAllCogsFormulas(tenantId);
        return ResponseEntity.ok(cogsFormulas);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getCogsFormulaById(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID cogsFormulaId) {

        permissionEvaluator.getCogsFormulaByIdPermission(tenantId, cogsFormulaId);

        return ResponseEntity.ok(cogsFormulaService.getCogsFormulaById(tenantId, cogsFormulaId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCogsFormula(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID cogsFormulaId,
            @RequestBody CogsFormulaRequest request) {

        permissionEvaluator.updateCogsFormulaPermission(tenantId);

        return ResponseEntity.ok(cogsFormulaService.updateCogsFormula(tenantId, cogsFormulaId, request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCogsFormula(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID cogsFormulaId) {

        permissionEvaluator.deleteCogsFormulaPermission(tenantId);

        cogsFormulaService.deleteCogsFormula(tenantId, cogsFormulaId);
        return ResponseEntity.ok("Deleted successfully");
    }
}
