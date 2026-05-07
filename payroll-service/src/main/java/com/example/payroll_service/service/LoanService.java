package com.example.payroll_service.service;

import com.example.payroll_service.dto.request.LoanRequest;
import com.example.payroll_service.dto.response.LoanResponse;
import com.example.payroll_service.enums.LoanStatus;
import com.example.payroll_service.exception.InvalidPayrollStateException;
import com.example.payroll_service.exception.LoanException;
import com.example.payroll_service.exception.ResourceExistsException;
import com.example.payroll_service.exception.ResourceNotFoundException;
import com.example.payroll_service.event.LoanEventProducer;
import com.example.payroll_service.mapper.EmployeeLoanMapper;
import com.example.payroll_service.model.EmployeeLoan;
import com.example.payroll_service.repository.EmployeeLoanRepository;
import com.example.payroll_service.utility.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final EmployeeLoanRepository employeeLoanRepository;
    private final EmployeeLoanMapper employeeLoanMapper;
    private final ValidationUtil validationUtil;
    private final LoanEventProducer loanEventProducer;

    @Transactional
    public LoanResponse createLoan(UUID tenantId, LoanRequest request, String username) {
        validationUtil.validateTenantAccess(tenantId);

        if (employeeLoanRepository.existsByTenantIdAndEmployeeId(tenantId, request.getEmployeeId())) {
            throw new ResourceExistsException("EmployeeLoan", "employeeId", request.getEmployeeId());
        }

        EmployeeLoan loan = employeeLoanMapper.mapToEntity(request, tenantId);
        if (loan.getStatus() == null) {
            loan.setStatus(LoanStatus.ACTIVE);
        }
        if (loan.getRemainingBalance() == null) {
            loan.setRemainingBalance(loan.getLoanAmount());
        }
        loan.setCreatedBy(username);
        loan.setCreatedByUsername(username);
        loan = employeeLoanRepository.save(loan);

        loanEventProducer.sendLoanCreatedEvent(employeeLoanMapper.mapToEvent(loan));

        log.info("Loan created with id: {} for employee: {}", loan.getId(), loan.getEmployeeId());
        return employeeLoanMapper.mapToDto(loan);
    }

    @Transactional
    public LoanResponse updateLoan(UUID tenantId, UUID id, LoanRequest request, String username) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        EmployeeLoan loan = validationUtil.getLoanById(tenantId, id);

        if (loan.getStatus() == LoanStatus.COMPLETED) {
            throw new InvalidPayrollStateException("Cannot update completed loan");
        }

        if (employeeLoanRepository.existsByTenantIdAndEmployeeId(tenantId, request.getEmployeeId()) 
                && !loan.getEmployeeId().equals(request.getEmployeeId())) {
            throw new ResourceExistsException("EmployeeLoan", "employeeId", request.getEmployeeId());
        }

        loan = employeeLoanMapper.updateEntity(loan, request);
        loan.setUpdatedBy(username);
        loan.setUpdatedByUsername(username);
        loan = employeeLoanRepository.save(loan);

        loanEventProducer.sendLoanUpdatedEvent(employeeLoanMapper.mapToEvent(loan));

        log.info("Loan updated with id: {}", id);
        return employeeLoanMapper.mapToDto(loan);
    }

    @Transactional
    public void deleteLoan(UUID tenantId, UUID id) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        EmployeeLoan loan = validationUtil.getLoanById(tenantId, id);

        if (loan.getStatus() == LoanStatus.ACTIVE) {
            throw new LoanException("Cannot delete active loan");
        }

        employeeLoanRepository.delete(loan);

        loanEventProducer.sendLoanUpdatedEvent(employeeLoanMapper.mapToEvent(loan));

        log.info("Loan deleted with id: {}", id);
    }

    public LoanResponse getLoanById(UUID tenantId, UUID id) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        EmployeeLoan loan = validationUtil.getLoanById(tenantId, id);
        return employeeLoanMapper.mapToDto(loan);
    }

    public List<LoanResponse> getAllLoans(UUID tenantId) {
        validationUtil.validateTenantAccess(tenantId);
        List<EmployeeLoan> loans = employeeLoanRepository.findByTenantId(tenantId);
        return loans.stream()
                .map(employeeLoanMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<LoanResponse> getEmployeeLoans(UUID tenantId, UUID employeeId) {
        validationUtil.validateTenantAccess(tenantId);
        List<EmployeeLoan> loans = employeeLoanRepository.findByTenantIdAndEmployeeId(tenantId, employeeId);
        return loans.stream()
                .map(employeeLoanMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<LoanResponse> getLoansByStatus(UUID tenantId, LoanStatus status) {
        validationUtil.validateTenantAccess(tenantId);
        List<EmployeeLoan> loans = employeeLoanRepository.findByTenantIdAndStatus(tenantId, status);
        return loans.stream()
                .map(employeeLoanMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanResponse stopLoan(UUID tenantId, UUID id, String username) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        EmployeeLoan loan = validationUtil.getLoanById(tenantId, id);

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new InvalidPayrollStateException("Cannot stop loan with status: " + loan.getStatus());
        }

        loan.setStopPayment(true);
        loan.setStatus(LoanStatus.STOPPED);
        loan.setUpdatedBy(username);
        loan.setUpdatedByUsername(username);
        loan = employeeLoanRepository.save(loan);

        loanEventProducer.sendLoanUpdatedEvent(employeeLoanMapper.mapToEvent(loan));

        log.info("Loan stopped with id: {}", id);
        return employeeLoanMapper.mapToDto(loan);
    }

    @Transactional
    public LoanResponse completeLoan(UUID tenantId, UUID id, String username) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        EmployeeLoan loan = validationUtil.getLoanById(tenantId, id);

        if (loan.getStatus() == LoanStatus.COMPLETED) {
            throw new InvalidPayrollStateException("Loan is already completed");
        }

        loan.setStatus(LoanStatus.COMPLETED);
        loan.setRemainingBalance(BigDecimal.ZERO);
        loan.setUpdatedBy(username);
        loan.setUpdatedByUsername(username);
        loan = employeeLoanRepository.save(loan);

        loanEventProducer.sendLoanUpdatedEvent(employeeLoanMapper.mapToEvent(loan));

        log.info("Loan completed with id: {}", id);
        return employeeLoanMapper.mapToDto(loan);
    }

    @Transactional
    public LoanResponse calculateInstallmentDeduction(UUID tenantId, UUID id, String username) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        EmployeeLoan loan = validationUtil.getLoanById(tenantId, id);

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new InvalidPayrollStateException("Cannot calculate deduction for loan with status: " + loan.getStatus());
        }

        if (Boolean.TRUE.equals(loan.getStopPayment())) {
            throw new LoanException("Loan payment is stopped");
        }

        BigDecimal deduction = loan.getInstallment();
        BigDecimal newBalance = loan.getRemainingBalance().subtract(deduction);

        if (newBalance.compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.COMPLETED);
            loan.setRemainingBalance(BigDecimal.ZERO);
        } else {
            loan.setRemainingBalance(newBalance);
        }

        loan.setUpdatedBy(username);
        loan.setUpdatedByUsername(username);
        loan = employeeLoanRepository.save(loan);

        loanEventProducer.sendLoanPaymentEvent(employeeLoanMapper.mapToEvent(loan));

        log.info("Loan installment deducted for loan id: {}, remaining balance: {}", id, loan.getRemainingBalance());
        return employeeLoanMapper.mapToDto(loan);
    }
}
