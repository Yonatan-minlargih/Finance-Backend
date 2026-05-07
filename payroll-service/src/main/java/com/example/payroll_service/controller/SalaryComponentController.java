package com.example.payroll_service.controller;

import com.example.payroll_service.dto.request.SalaryComponentRequest;
import com.example.payroll_service.dto.response.SalaryComponentResponse;
import com.example.payroll_service.service.SalaryComponentService;
import com.example.payroll_service.utility.PermissionEvaluator;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payroll/salary-components/{tenant-id}")
@RequiredArgsConstructor
@Tag(name = "Salary Component", description = "Employee salary component management endpoints")
public class SalaryComponentController {

    private final SalaryComponentService salaryComponentService;
    private final PermissionEvaluator permissionEvaluator;

    @PostMapping("/add")
    public ResponseEntity<?> addSalaryComponent(
            @PathVariable("tenant-id") UUID tenantId,
            @Valid @RequestBody SalaryComponentRequest request) {

        // permissionEvaluator.addSalaryComponentPermission(tenantId);

        SalaryComponentResponse response = salaryComponentService.assignComponentToEmployee(tenantId, request, "system");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllSalaryComponents(
            @PathVariable("tenant-id") UUID tenantId) {

        // permissionEvaluator.getSalaryComponentPermission(tenantId);

        List<SalaryComponentResponse> response = salaryComponentService.getAllSalaryComponents(tenantId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getSalaryComponentById(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID id) {

        // permissionEvaluator.getSalaryComponentPermission(tenantId);

        SalaryComponentResponse response = salaryComponentService.getSalaryComponentById(tenantId, id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/employee/{employee-id}")
    public ResponseEntity<?> getEmployeeSalaryComponents(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("employee-id") UUID employeeId) {

        // permissionEvaluator.getSalaryComponentPermission(tenantId);

        List<SalaryComponentResponse> response = salaryComponentService.getEmployeeComponents(tenantId, employeeId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/employee/{employee-id}/active")
    public ResponseEntity<?> getActiveEmployeeComponents(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("employee-id") UUID employeeId) {

        // permissionEvaluator.getSalaryComponentPermission(tenantId);

        List<SalaryComponentResponse> response = salaryComponentService.getActiveEmployeeComponents(tenantId, employeeId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/type/{type-id}")
    public ResponseEntity<?> getComponentsByType(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("type-id") UUID typeId) {

        // permissionEvaluator.getSalaryComponentPermission(tenantId);

        List<SalaryComponentResponse> response = salaryComponentService.getComponentsByType(tenantId, typeId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/is-active/{is-active}")
    public ResponseEntity<?> getComponentsByIsActive(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("is-active") Boolean isActive) {

        // permissionEvaluator.getSalaryComponentPermission(tenantId);

        List<SalaryComponentResponse> response = salaryComponentService.getComponentsByIsActive(tenantId, isActive);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateSalaryComponent(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID id,
            @Valid @RequestBody SalaryComponentRequest request) {

        // permissionEvaluator.updateSalaryComponentPermission(tenantId);

        SalaryComponentResponse response = salaryComponentService.updateSalaryComponent(tenantId, id, request, "system");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/activate/{id}")
    public ResponseEntity<?> activateComponent(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID id) {

        // permissionEvaluator.updateSalaryComponentPermission(tenantId);

        SalaryComponentResponse response = salaryComponentService.activateComponent(tenantId, id, "system");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/deactivate/{id}")
    public ResponseEntity<?> deactivateComponent(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID id) {

        // permissionEvaluator.updateSalaryComponentPermission(tenantId);

        SalaryComponentResponse response = salaryComponentService.deactivateComponent(tenantId, id, "system");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteSalaryComponent(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("id") UUID id) {

        // permissionEvaluator.deleteSalaryComponentPermission(tenantId);

        salaryComponentService.deleteSalaryComponent(tenantId, id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
