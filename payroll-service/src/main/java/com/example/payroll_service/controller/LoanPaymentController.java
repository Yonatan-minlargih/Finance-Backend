package com.example.payroll_service.controller;

import com.example.payroll_service.dto.response.LoanPaymentResponse;
import com.example.payroll_service.service.LoanPaymentService;
import com.example.payroll_service.utility.PermissionEvaluator;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payroll/loan-payments/{tenant-id}")
@RequiredArgsConstructor
@Tag(name = "Loan Payment", description = "Loan payment management endpoints")
public class LoanPaymentController {

    private final LoanPaymentService loanPaymentService;
    private final PermissionEvaluator permissionEvaluator;

    @PostMapping("/add")
    public ResponseEntity<?> createLoanPayment(
            @PathVariable("tenant-id") UUID tenantId,
            @RequestParam UUID loanId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) LocalDate paymentDate,
            @RequestParam(required = false) UUID payrollRunId) {

        // permissionEvaluator.getLoanPaymentPermission(tenantId);

        LoanPaymentResponse response = loanPaymentService.createLoanPayment(
                tenantId, loanId, amount, paymentDate, payrollRunId, "system");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/update/{payment-id}")
    public ResponseEntity<?> updateLoanPayment(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("payment-id") UUID paymentId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) LocalDate paymentDate) {

        // permissionEvaluator.getLoanPaymentPermission(tenantId);

        LoanPaymentResponse response = loanPaymentService.updateLoanPayment(
                tenantId, paymentId, amount, paymentDate, "system");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{payment-id}")
    public ResponseEntity<?> deleteLoanPayment(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("payment-id") UUID paymentId) {

        // permissionEvaluator.getLoanPaymentPermission(tenantId);

        loanPaymentService.deleteLoanPayment(tenantId, paymentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllLoanPayments(
            @PathVariable("tenant-id") UUID tenantId) {

        // permissionEvaluator.getLoanPaymentPermission(tenantId);

        List<LoanPaymentResponse> response = loanPaymentService.getAllLoanPayments(tenantId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/{payment-id}")
    public ResponseEntity<?> getLoanPaymentById(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("payment-id") UUID paymentId) {

        // permissionEvaluator.getLoanPaymentPermission(tenantId);

        LoanPaymentResponse response = loanPaymentService.getLoanPaymentById(tenantId, paymentId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/loan/{loan-id}")
    public ResponseEntity<?> getLoanPaymentsByLoan(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("loan-id") UUID loanId) {

        // permissionEvaluator.getLoanPaymentPermission(tenantId);

        List<LoanPaymentResponse> response = loanPaymentService.getLoanPaymentsByLoan(tenantId, loanId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/run/{run-id}")
    public ResponseEntity<?> getLoanPaymentsByPayrollRun(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("run-id") UUID runId) {

        // permissionEvaluator.getLoanPaymentPermission(tenantId);

        List<LoanPaymentResponse> response = loanPaymentService.getLoanPaymentsByPayrollRun(tenantId, runId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get/loan/{loan-id}/run/{run-id}")
    public ResponseEntity<?> getLoanPaymentsByLoanAndRun(
            @PathVariable("tenant-id") UUID tenantId,
            @PathVariable("loan-id") UUID loanId,
            @PathVariable("run-id") UUID runId) {

        // permissionEvaluator.getLoanPaymentPermission(tenantId);

        List<LoanPaymentResponse> response = loanPaymentService.getLoanPaymentsByLoanAndPayrollRun(
                tenantId, loanId, runId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
