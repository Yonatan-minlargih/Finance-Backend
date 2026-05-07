package com.example.payroll_service.utility;

import com.example.payroll_service.enums.LoanStatus;
import com.example.payroll_service.enums.PayrollStatus;
import com.example.payroll_service.exception.InvalidPayrollStateException;
import com.example.payroll_service.exception.ResourceNotFoundException;
import com.example.payroll_service.model.EmployeeLoan;
import com.example.payroll_service.model.EarningDeductionType;
import com.example.payroll_service.model.PayrollDetail;
import com.example.payroll_service.model.PayrollRun;
import com.example.payroll_service.repository.EmployeeLoanRepository;
import com.example.payroll_service.repository.EarningDeductionTypeRepository;
import com.example.payroll_service.repository.PayrollDetailRepository;
import com.example.payroll_service.repository.PayrollRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ValidationUtil {

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollDetailRepository payrollDetailRepository;
    private final EmployeeLoanRepository employeeLoanRepository;
    private final EarningDeductionTypeRepository earningDeductionTypeRepository;

    public PayrollRun getPayrollRunById(UUID tenantId, UUID id) {
        return payrollRunRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", id));
    }

    public PayrollDetail getPayrollDetailById(UUID tenantId, UUID id) {
        return payrollDetailRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollDetail", "id", id));
    }

    public EmployeeLoan getLoanById(UUID tenantId, UUID id) {
        return employeeLoanRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeLoan", "id", id));
    }

    public EmployeeLoan getActiveLoanByEmployee(UUID tenantId, UUID employeeId) {
        return employeeLoanRepository.findByTenantIdAndEmployeeId(tenantId, employeeId)
                .stream()
                .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Active EmployeeLoan", "employeeId", employeeId));
    }

    public EarningDeductionType getEarningTypeById(UUID tenantId, UUID id) {
        return earningDeductionTypeRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("EarningDeductionType", "id", id));
    }

    public void validateTenantAccess(UUID tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
    }

    public void validatePayrollState(PayrollRun payrollRun, PayrollStatus expectedState) {
        if (payrollRun.getStatus() != expectedState) {
            throw new InvalidPayrollStateException(payrollRun.getStatus().name(), expectedState.name());
        }
    }

    public void validatePayrollStateForProcessing(PayrollRun payrollRun) {
        if (payrollRun.getStatus() != PayrollStatus.DRAFT && payrollRun.getStatus() != PayrollStatus.CANCELLED) {
            throw new InvalidPayrollStateException(
                    "Payroll can only be processed from DRAFT or CANCELLED state. Current state: " + payrollRun.getStatus()
            );
        }
    }

    public void validatePayrollStateForApproval(PayrollRun payrollRun) {
        if (payrollRun.getStatus() != PayrollStatus.PROCESSED) {
            throw new InvalidPayrollStateException(
                    "Payroll can only be approved from PROCESSED state. Current state: " + payrollRun.getStatus()
            );
        }
    }

    public void validatePayrollStateForPosting(PayrollRun payrollRun) {
        if (payrollRun.getStatus() != PayrollStatus.APPROVED) {
            throw new InvalidPayrollStateException(
                    "Payroll can only be posted from APPROVED state. Current state: " + payrollRun.getStatus()
            );
        }
    }
}