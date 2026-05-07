package com.example.payroll_service.controller;

import com.example.payroll_service.dto.request.LoanRequest;
import com.example.payroll_service.dto.response.LoanResponse;
import com.example.payroll_service.enums.LoanStatus;
import com.example.payroll_service.service.LoanService;
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
@RequestMapping("/api/payroll/loans/{tenant-id}")
@RequiredArgsConstructor
@Tag(name = "Employee Loan", description = "Employee loan management endpoints")
public class EmployeeLoanController {

    private final LoanService loanService;
    private final PermissionEvaluator permissionEvaluator;

    @PostMapping("/add")
    public ResponseEntity<?> addLoan(
            @PathVariable("tenant-id") UUID tenantId,
            @Valid @RequestBody LoanRequest request) {

        // permissionEvaluator.addLoanPermission(tenantId);

        LoanResponse response = loanService.createLoan(tenantId, request, "system");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllLoans(
            @PathVariable("tenant-id") UUID tenantId) {

        // permissionEvaluator.getLoanPermission(tenantId);

        List<LoanResponse> response = loanService.getAllLoans(tenantId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/{loan-id}")
    public ResponseEntity<?> getLoanById(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("loan-id") UUID loanId) {

        // permissionEvaluator.getLoanPermission(tenantId);

        LoanResponse response = loanService.getLoanById(tenantId, loanId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/employee/{employee-id}")
    public ResponseEntity<?> getEmployeeLoans(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("employee-id") UUID employeeId) {

        // permissionEvaluator.getLoanPermission(tenantId);

        List<LoanResponse> response = loanService.getEmployeeLoans(tenantId, employeeId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/status/{status}")
    public ResponseEntity<?> getLoansByStatus(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("status") LoanStatus status) {

        // permissionEvaluator.getLoanPermission(tenantId);

        List<LoanResponse> response = loanService.getLoansByStatus(tenantId, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/update/{loan-id}")
    public ResponseEntity<?> updateLoan(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("loan-id") UUID loanId,
            @Valid @RequestBody LoanRequest request) {

        // permissionEvaluator.updateLoanPermission(tenantId);

        LoanResponse response = loanService.updateLoan(tenantId, loanId, request, "system");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/stop/{loan-id}")
    public ResponseEntity<?> stopLoan(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("loan-id") UUID loanId) {

        // permissionEvaluator.stopLoanPermission(tenantId);

        LoanResponse response = loanService.stopLoan(tenantId, loanId, "system");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/complete/{loan-id}")
    public ResponseEntity<?> completeLoan(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("loan-id") UUID loanId) {

        // permissionEvaluator.resumeLoanPermission(tenantId);

        LoanResponse response = loanService.completeLoan(tenantId, loanId, "system");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/resume/{loan-id}")
    public ResponseEntity<?> resumeLoan(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("loan-id") UUID loanId) {

        // permissionEvaluator.resumeLoanPermission(tenantId);

        LoanResponse response = loanService.calculateInstallmentDeduction(tenantId, loanId, "system");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{loan-id}")
    public ResponseEntity<?> deleteLoan(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("loan-id") UUID loanId) {

        // permissionEvaluator.deleteLoanPermission(tenantId);

        loanService.deleteLoan(tenantId, loanId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
