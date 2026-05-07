package com.example.payroll_service.controller;

import com.example.payroll_service.dto.request.PayrollRunRequest;
import com.example.payroll_service.dto.response.PayrollRunResponse;
import com.example.payroll_service.enums.PayrollStatus;
import com.example.payroll_service.service.PayrollRunService;
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
@RequestMapping("/api/payroll/runs/{tenant-id}")
@RequiredArgsConstructor
@Tag(name = "Payroll Run", description = "Payroll run management endpoints")
public class PayrollRunController {

    private final PayrollRunService payrollRunService;
    private final PermissionEvaluator permissionEvaluator;

    @PostMapping("/add")
    public ResponseEntity<?> addPayrollRun(
            @PathVariable("tenant-id") UUID tenantId,
            @Valid @RequestBody PayrollRunRequest request) {

        // permissionEvaluator.addPayrollRunPermission(tenantId);

        PayrollRunResponse response = payrollRunService.createPayrollRun(tenantId, request, "system");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/update/{run-id}")
    public ResponseEntity<?> updatePayrollRun(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("run-id") UUID runId,
            @Valid @RequestBody PayrollRunRequest request) {

        // permissionEvaluator.updatePayrollRunPermission(tenantId);

        PayrollRunResponse response = payrollRunService.updatePayrollRun(tenantId, runId, request, "system");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllPayrollRuns(
            @PathVariable("tenant-id") UUID tenantId) {

        // permissionEvaluator.getPayrollRunPermission(tenantId);

        List<PayrollRunResponse> response = payrollRunService.getAllPayrollRuns(tenantId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/{run-id}")
    public ResponseEntity<?> getPayrollRunById(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("run-id") UUID runId) {

        // permissionEvaluator.getPayrollRunPermission(tenantId);

        PayrollRunResponse response = payrollRunService.getPayrollRunById(tenantId, runId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/status/{status}")
    public ResponseEntity<?> getPayrollRunsByStatus(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("status") PayrollStatus status) {

        // permissionEvaluator.getPayrollRunPermission(tenantId);

        List<PayrollRunResponse> response = payrollRunService.getPayrollRunsByStatus(tenantId, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/date-range")
    public ResponseEntity<?> getPayrollRunsByDateRange(
            @PathVariable("tenant-id") UUID tenantId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        // permissionEvaluator.getPayrollRunPermission(tenantId);

        List<PayrollRunResponse> response = payrollRunService.getPayrollRunsByDateRange(tenantId, startDate, endDate);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchByFiscalPeriod(
            @PathVariable("tenant-id") UUID tenantId,
            @RequestParam String keyword) {

        // permissionEvaluator.getPayrollRunPermission(tenantId);

        List<PayrollRunResponse> response = payrollRunService.searchByFiscalPeriod(tenantId, keyword);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/process/{run-id}")
    public ResponseEntity<?> processPayroll(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("run-id") UUID runId) {

        // permissionEvaluator.processPayrollPermission(tenantId);

        PayrollRunResponse response = payrollRunService.processPayroll(tenantId, runId, "system");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/approve/{run-id}")
    public ResponseEntity<?> approvePayroll(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("run-id") UUID runId) {

        // permissionEvaluator.approvePayrollPermission(tenantId);

        PayrollRunResponse response = payrollRunService.approvePayroll(tenantId, runId, "system");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/post/{run-id}")
    public ResponseEntity<?> postPayroll(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("run-id") UUID runId) {

        // permissionEvaluator.postPayrollPermission(tenantId);

        PayrollRunResponse response = payrollRunService.postPayroll(tenantId, runId, "system");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/cancel/{run-id}")
    public ResponseEntity<?> cancelPayroll(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("run-id") UUID runId) {

        // permissionEvaluator.cancelPayrollPermission(tenantId);

        PayrollRunResponse response = payrollRunService.cancelPayroll(tenantId, runId, "system");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{run-id}")
    public ResponseEntity<?> deletePayrollRun(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("run-id") UUID runId) {

        // permissionEvaluator.deletePayrollRunPermission(tenantId);

        payrollRunService.deletePayrollRun(tenantId, runId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
