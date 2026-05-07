package com.example.payroll_service.controller;

import com.example.payroll_service.dto.request.EarningDeductionTypeRequest;
import com.example.payroll_service.dto.response.EarningDeductionTypeResponse;
import com.example.payroll_service.enums.CalculationMethod;
import com.example.payroll_service.enums.EarningDeductionCategory;
import com.example.payroll_service.service.EarningDeductionTypeService;
import com.example.payroll_service.utility.PermissionEvaluator;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payroll/types/{tenant-id}")
@RequiredArgsConstructor
@Tag(name = "Earning Deduction Type", description = "Earning and deduction type management endpoints")
public class EarningDeductionTypeController {

    private final EarningDeductionTypeService earningDeductionTypeService;
    private final PermissionEvaluator permissionEvaluator;

    @PostMapping("/add")
    public ResponseEntity<?> addEarningDeductionType(
            @PathVariable("tenant-id") UUID tenantId,
            @Valid @RequestBody EarningDeductionTypeRequest request) {

        // permissionEvaluator.addEarningDeductionTypePermission(tenantId);

        EarningDeductionTypeResponse response = earningDeductionTypeService.createType(tenantId, request, "system");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllEarningDeductionTypes(
            @PathVariable("tenant-id") UUID tenantId) {

        // permissionEvaluator.getEarningDeductionTypePermission(tenantId);

        List<EarningDeductionTypeResponse> response = earningDeductionTypeService.getAllTypes(tenantId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getEarningDeductionTypeById(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID id) {

        // permissionEvaluator.getEarningDeductionTypePermission(tenantId);

        EarningDeductionTypeResponse response = earningDeductionTypeService.getTypeById(tenantId, id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/category/{category}")
    public ResponseEntity<?> getTypesByCategory(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("category") EarningDeductionCategory category) {

        // permissionEvaluator.getEarningDeductionTypePermission(tenantId);

        List<EarningDeductionTypeResponse> response = earningDeductionTypeService.getTypesByCategory(tenantId, category);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/earnings")
    public ResponseEntity<?> getEarningTypes(
            @PathVariable("tenant-id") UUID tenantId) {

        // permissionEvaluator.getEarningDeductionTypePermission(tenantId);

        List<EarningDeductionTypeResponse> response = earningDeductionTypeService.getEarningTypes(tenantId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/deductions")
    public ResponseEntity<?> getDeductionTypes(
            @PathVariable("tenant-id") UUID tenantId) {

        // permissionEvaluator.getEarningDeductionTypePermission(tenantId);

        List<EarningDeductionTypeResponse> response = earningDeductionTypeService.getDeductionTypes(tenantId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/calculation-method/{method}")
    public ResponseEntity<?> getTypesByCalculationMethod(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("method") CalculationMethod method) {

        // permissionEvaluator.getEarningDeductionTypePermission(tenantId);

        List<EarningDeductionTypeResponse> response = earningDeductionTypeService.getTypesByCalculationMethod(tenantId, method);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/overtime-eligible")
    public ResponseEntity<?> getOvertimeEligibleTypes(
            @PathVariable("tenant-id") UUID tenantId) {

        // permissionEvaluator.getEarningDeductionTypePermission(tenantId);

        List<EarningDeductionTypeResponse> response = earningDeductionTypeService.getOvertimeEligibleTypes(tenantId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchByTypeName(
            @PathVariable("tenant-id") UUID tenantId,
            @RequestParam String keyword) {

        // permissionEvaluator.getEarningDeductionTypePermission(tenantId);

        List<EarningDeductionTypeResponse> response = earningDeductionTypeService.searchByTypeName(tenantId, keyword);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/active")
    public ResponseEntity<?> getActiveTypes(
            @PathVariable("tenant-id") UUID tenantId,
            @RequestParam LocalDate currentDate) {

        // permissionEvaluator.getEarningDeductionTypePermission(tenantId);

        List<EarningDeductionTypeResponse> response = earningDeductionTypeService.getActiveTypes(tenantId, currentDate);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEarningDeductionType(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID id,
            @Valid @RequestBody EarningDeductionTypeRequest request) {

        // permissionEvaluator.updateEarningDeductionTypePermission(tenantId);

        EarningDeductionTypeResponse response = earningDeductionTypeService.updateType(tenantId, id, request, "system");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEarningDeductionType(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID id) {

        // permissionEvaluator.deleteEarningDeductionTypePermission(tenantId);

        earningDeductionTypeService.deleteType(tenantId, id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
