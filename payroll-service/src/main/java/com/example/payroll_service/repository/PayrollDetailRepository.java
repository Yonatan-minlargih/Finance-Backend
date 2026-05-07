package com.example.payroll_service.repository;

import com.example.payroll_service.model.PayrollDetail;
import com.example.payroll_service.model.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollDetailRepository extends JpaRepository<PayrollDetail, UUID> {

    List<PayrollDetail> findByTenantId(UUID tenantId);

    Optional<PayrollDetail> findByTenantIdAndId(UUID tenantId, UUID id);

    List<PayrollDetail> findByTenantIdAndPayrollRun(UUID tenantId, PayrollRun payrollRun);

    List<PayrollDetail> findByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    List<PayrollDetail> findByTenantIdAndPayrollRunAndEmployeeId(UUID tenantId, PayrollRun payrollRun, UUID employeeId);
}
