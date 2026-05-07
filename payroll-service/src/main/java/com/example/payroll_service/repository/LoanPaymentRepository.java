package com.example.payroll_service.repository;

import com.example.payroll_service.model.EmployeeLoan;
import com.example.payroll_service.model.LoanPayment;
import com.example.payroll_service.model.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, UUID> {

    List<LoanPayment> findByTenantId(UUID tenantId);

    Optional<LoanPayment> findByTenantIdAndId(UUID tenantId, UUID id);

    List<LoanPayment> findByTenantIdAndLoan(UUID tenantId, EmployeeLoan loan);

    List<LoanPayment> findByTenantIdAndPayrollRun(UUID tenantId, PayrollRun payrollRun);

    List<LoanPayment> findByTenantIdAndLoanAndPayrollRun(UUID tenantId, EmployeeLoan loan, PayrollRun payrollRun);
}
