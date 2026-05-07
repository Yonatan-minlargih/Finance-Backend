package com.example.payroll_service.mapper;

import com.example.payroll_service.dto.eventDto.LoanEventDto;
import com.example.payroll_service.dto.response.LoanPaymentResponse;
import com.example.payroll_service.model.LoanPayment;
import org.springframework.stereotype.Component;

@Component
public class LoanPaymentMapper {

    public LoanPaymentResponse mapToDto(LoanPayment payment) {
        return LoanPaymentResponse.builder()
                .id(payment.getId())
                .tenantId(payment.getTenantId())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .loanId(payment.getLoan() != null ? payment.getLoan().getId() : null)
                .payrollRunId(payment.getPayrollRun() != null ? payment.getPayrollRun().getId() : null)
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .createdBy(payment.getCreatedBy())
                .updatedBy(payment.getUpdatedBy())
                .build();
    }

    public LoanEventDto mapToEvent(LoanPayment payment) {
        return LoanEventDto.builder()
                .id(payment.getId())
                .tenantId(payment.getTenantId())
                .eventType("LOAN_PAYMENT")
                .employeeId(payment.getLoan() != null ? payment.getLoan().getEmployeeId() : null)
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
