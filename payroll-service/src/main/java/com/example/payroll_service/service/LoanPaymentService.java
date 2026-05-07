package com.example.payroll_service.service;

import com.example.payroll_service.dto.response.LoanPaymentResponse;
import com.example.payroll_service.enums.LoanStatus;
import com.example.payroll_service.exception.InvalidPayrollStateException;
import com.example.payroll_service.exception.LoanException;
import com.example.payroll_service.exception.ResourceNotFoundException;
import com.example.payroll_service.event.LoanEventProducer;
import com.example.payroll_service.mapper.LoanPaymentMapper;
import com.example.payroll_service.model.EmployeeLoan;
import com.example.payroll_service.model.LoanPayment;
import com.example.payroll_service.model.PayrollRun;
import com.example.payroll_service.repository.EmployeeLoanRepository;
import com.example.payroll_service.repository.LoanPaymentRepository;
import com.example.payroll_service.repository.PayrollRunRepository;
import com.example.payroll_service.utility.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanPaymentService {

    private final LoanPaymentRepository loanPaymentRepository;
    private final EmployeeLoanRepository employeeLoanRepository;
    private final PayrollRunRepository payrollRunRepository;
    private final LoanPaymentMapper loanPaymentMapper;
    private final ValidationUtil validationUtil;
    private final LoanEventProducer loanEventProducer;

    @Transactional
    public LoanPaymentResponse createLoanPayment(UUID tenantId, UUID loanId, BigDecimal amount, 
                                                  LocalDate paymentDate, UUID payrollRunId, String username) {
        validationUtil.validateTenantAccess(tenantId);
        EmployeeLoan loan = validationUtil.getLoanById(tenantId, loanId);

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new InvalidPayrollStateException("Cannot apply payment for loan with status: " + loan.getStatus());
        }

        if (Boolean.TRUE.equals(loan.getStopPayment())) {
            throw new LoanException("Loan payment is stopped");
        }

        LoanPayment payment = new LoanPayment();
        payment.setAmount(amount);
        payment.setPaymentDate(paymentDate != null ? paymentDate : LocalDate.now());
        payment.setLoan(loan);
        payment.setTenantId(tenantId);

        if (payrollRunId != null) {
            PayrollRun payrollRun = payrollRunRepository.findByTenantIdAndId(tenantId, payrollRunId)
                    .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", payrollRunId));
            payment.setPayrollRun(payrollRun);
        }

        payment.setCreatedBy(username);
        payment.setCreatedByUsername(username);
        payment = loanPaymentRepository.save(payment);

        BigDecimal newBalance = loan.getRemainingBalance().subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.COMPLETED);
            loan.setRemainingBalance(BigDecimal.ZERO);
        } else {
            loan.setRemainingBalance(newBalance);
        }

        loan.setUpdatedBy(username);
        loan.setUpdatedByUsername(username);
        employeeLoanRepository.save(loan);

        loanEventProducer.sendLoanPaymentEvent(loanPaymentMapper.mapToEvent(payment));

        log.info("Loan payment created with id: {} for loan: {}", payment.getId(), loan.getId());
        return loanPaymentMapper.mapToDto(payment);
    }

    @Transactional
    public LoanPaymentResponse updateLoanPayment(UUID tenantId, UUID id, BigDecimal amount, 
                                                  LocalDate paymentDate, String username) {
        validationUtil.validateTenantAccess(tenantId);
        LoanPayment payment = loanPaymentRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanPayment", "id", id));

        payment.setAmount(amount);
        payment.setPaymentDate(paymentDate);
        payment.setUpdatedBy(username);
        payment.setUpdatedByUsername(username);
        payment = loanPaymentRepository.save(payment);

        loanEventProducer.sendLoanPaymentEvent(loanPaymentMapper.mapToEvent(payment));

        log.info("Loan payment updated with id: {}", id);
        return loanPaymentMapper.mapToDto(payment);
    }

    @Transactional
    public void deleteLoanPayment(UUID tenantId, UUID id) {
        validationUtil.validateTenantAccess(tenantId);
        LoanPayment payment = loanPaymentRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanPayment", "id", id));

        loanPaymentRepository.delete(payment);

        loanEventProducer.sendLoanPaymentEvent(loanPaymentMapper.mapToEvent(payment));

        log.info("Loan payment deleted with id: {}", id);
    }

    public LoanPaymentResponse getLoanPaymentById(UUID tenantId, UUID id) {
        validationUtil.validateTenantAccess(tenantId);
        LoanPayment payment = loanPaymentRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanPayment", "id", id));
        return loanPaymentMapper.mapToDto(payment);
    }

    public List<LoanPaymentResponse> getAllLoanPayments(UUID tenantId) {
        validationUtil.validateTenantAccess(tenantId);
        List<LoanPayment> payments = loanPaymentRepository.findByTenantId(tenantId);
        return payments.stream()
                .map(loanPaymentMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<LoanPaymentResponse> getLoanPaymentsByLoan(UUID tenantId, UUID loanId) {
        validationUtil.validateTenantAccess(tenantId);
        EmployeeLoan loan = validationUtil.getLoanById(tenantId, loanId);
        List<LoanPayment> payments = loanPaymentRepository.findByTenantIdAndLoan(tenantId, loan);
        return payments.stream()
                .map(loanPaymentMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<LoanPaymentResponse> getLoanPaymentsByPayrollRun(UUID tenantId, UUID payrollRunId) {
        validationUtil.validateTenantAccess(tenantId);
        PayrollRun payrollRun = payrollRunRepository.findByTenantIdAndId(tenantId, payrollRunId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", payrollRunId));
        List<LoanPayment> payments = loanPaymentRepository.findByTenantIdAndPayrollRun(tenantId, payrollRun);
        return payments.stream()
                .map(loanPaymentMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<LoanPaymentResponse> getLoanPaymentsByLoanAndPayrollRun(UUID tenantId, UUID loanId, UUID payrollRunId) {
        validationUtil.validateTenantAccess(tenantId);
        EmployeeLoan loan = validationUtil.getLoanById(tenantId, loanId);
        PayrollRun payrollRun = payrollRunRepository.findByTenantIdAndId(tenantId, payrollRunId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", payrollRunId));
        List<LoanPayment> payments = loanPaymentRepository.findByTenantIdAndLoanAndPayrollRun(tenantId, loan, payrollRun);
        return payments.stream()
                .map(loanPaymentMapper::mapToDto)
                .collect(Collectors.toList());
    }
}
