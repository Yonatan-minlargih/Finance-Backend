package com.example.payroll_service.controller;

import com.example.payroll_service.dto.response.PayrollDetailResponse;
import com.example.payroll_service.service.PayrollDetailService;
import com.example.payroll_service.utility.PermissionEvaluator;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payroll/details/{tenant-id}")
@RequiredArgsConstructor
@Tag(name = "Payroll Detail", description = "Payroll detail management endpoints")
public class PayrollDetailController {

    private final PayrollDetailService payrollDetailService;
    private final PermissionEvaluator permissionEvaluator;

    @PostMapping("/add")
    public ResponseEntity<?> createPayrollDetail(
            @PathVariable("tenant-id") UUID tenantId,
            @RequestParam UUID employeeId,
            @RequestParam BigDecimal basicSalary,
            @RequestParam BigDecimal overtime,
            @RequestParam BigDecimal bonus,
            @RequestParam UUID payrollRunId) {

        // permissionEvaluator.getPayrollDetailPermission(tenantId);

        PayrollDetailResponse response = payrollDetailService.createPayrollDetail(
                tenantId, employeeId, basicSalary, overtime, bonus, payrollRunId, "system");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/update/{detail-id}")
    public ResponseEntity<?> updatePayrollDetail(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("detail-id") UUID detailId,
            @RequestParam BigDecimal basicSalary,
            @RequestParam BigDecimal overtime,
            @RequestParam BigDecimal bonus) {

        // permissionEvaluator.getPayrollDetailPermission(tenantId);

        PayrollDetailResponse response = payrollDetailService.updatePayrollDetail(
                tenantId, detailId, basicSalary, overtime, bonus, "system");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{detail-id}")
    public ResponseEntity<?> deletePayrollDetail(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("detail-id") UUID detailId) {

        // permissionEvaluator.getPayrollDetailPermission(tenantId);

        payrollDetailService.deletePayrollDetail(tenantId, detailId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllPayrollDetails(
            @PathVariable("tenant-id") UUID tenantId) {

        // permissionEvaluator.getPayrollDetailPermission(tenantId);

        List<PayrollDetailResponse> response = payrollDetailService.getAllPayrollDetails(tenantId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/{detail-id}")
    public ResponseEntity<?> getPayrollDetailById(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("detail-id") UUID detailId) {

        // permissionEvaluator.getPayrollDetailPermission(tenantId);

        PayrollDetailResponse response = payrollDetailService.getPayrollDetailById(tenantId, detailId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/run/{run-id}")
    public ResponseEntity<?> getPayrollDetailsByRun(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("run-id") UUID runId) {

        // permissionEvaluator.getPayrollDetailPermission(tenantId);

        List<PayrollDetailResponse> response = payrollDetailService.getPayrollDetailsByPayrollRun(tenantId, runId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/employee/{employee-id}")
    public ResponseEntity<?> getPayrollDetailsByEmployee(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("employee-id") UUID employeeId) {

        // permissionEvaluator.getPayrollDetailPermission(tenantId);

        List<PayrollDetailResponse> response = payrollDetailService.getPayrollDetailsByEmployee(tenantId, employeeId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/employee/{employee-id}/run/{run-id}")
    public ResponseEntity<?> getPayrollDetailsByEmployeeAndRun(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("employee-id") UUID employeeId,
            @PathVariable("run-id") UUID runId) {

        // permissionEvaluator.getPayrollDetailPermission(tenantId);

        List<PayrollDetailResponse> response = payrollDetailService.getPayrollDetailsByEmployeeAndPayrollRun(
                tenantId, runId, employeeId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
