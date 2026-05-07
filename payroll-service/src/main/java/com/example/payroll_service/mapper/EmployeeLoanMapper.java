package com.example.payroll_service.mapper;

import com.example.payroll_service.dto.eventDto.LoanEventDto;
import com.example.payroll_service.dto.request.LoanRequest;
import com.example.payroll_service.dto.response.LoanResponse;
import com.example.payroll_service.model.EmployeeLoan;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmployeeLoanMapper {

    public EmployeeLoan mapToEntity(LoanRequest request, UUID tenantId) {
        EmployeeLoan loan = new EmployeeLoan();
        loan.setEmployeeId(request.getEmployeeId());
        loan.setLoanAmount(request.getLoanAmount());
        loan.setInstallment(request.getInstallment());
        loan.setRemainingBalance(request.getRemainingBalance());
        loan.setInterestRate(request.getInterestRate());
        loan.setStartDate(request.getStartDate());
        loan.setEndDate(request.getEndDate());
        loan.setStopPayment(request.getStopPayment());
        loan.setStatus(request.getStatus());
        loan.setTenantId(tenantId);
        return loan;
    }

    public EmployeeLoan updateEntity(EmployeeLoan loan, LoanRequest request) {
        loan.setLoanAmount(request.getLoanAmount());
        loan.setInstallment(request.getInstallment());
        loan.setRemainingBalance(request.getRemainingBalance());
        loan.setInterestRate(request.getInterestRate());
        loan.setStartDate(request.getStartDate());
        loan.setEndDate(request.getEndDate());
        loan.setStopPayment(request.getStopPayment());
        loan.setStatus(request.getStatus());
        return loan;
    }

    public LoanResponse mapToDto(EmployeeLoan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .tenantId(loan.getTenantId())
                .employeeId(loan.getEmployeeId())
                .loanAmount(loan.getLoanAmount())
                .installment(loan.getInstallment())
                .remainingBalance(loan.getRemainingBalance())
                .interestRate(loan.getInterestRate())
                .startDate(loan.getStartDate())
                .endDate(loan.getEndDate())
                .stopPayment(loan.getStopPayment())
                .status(loan.getStatus())
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .createdBy(loan.getCreatedBy())
                .updatedBy(loan.getUpdatedBy())
                .build();
    }

    public LoanEventDto mapToEvent(EmployeeLoan loan) {
        return LoanEventDto.builder()
                .id(loan.getId())
                .tenantId(loan.getTenantId())
                .eventType("EMPLOYEE_LOAN")
                .employeeId(loan.getEmployeeId())
                .loanAmount(loan.getLoanAmount())
                .installment(loan.getInstallment())
                .remainingBalance(loan.getRemainingBalance())
                .status(loan.getStatus() != null ? loan.getStatus().name() : null)
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }
}
