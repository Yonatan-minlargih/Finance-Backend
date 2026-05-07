package com.example.payroll_service.repository;

import com.example.payroll_service.enums.LoanStatus;
import com.example.payroll_service.model.EmployeeLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeLoanRepository extends JpaRepository<EmployeeLoan, UUID> {

    List<EmployeeLoan> findByTenantId(UUID tenantId);

    Optional<EmployeeLoan> findByTenantIdAndId(UUID tenantId, UUID id);

    List<EmployeeLoan> findByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    List<EmployeeLoan> findByTenantIdAndStatus(UUID tenantId, LoanStatus status);

    boolean existsByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);
}
