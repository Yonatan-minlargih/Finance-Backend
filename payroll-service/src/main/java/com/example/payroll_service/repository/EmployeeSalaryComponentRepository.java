package com.example.payroll_service.repository;

import com.example.payroll_service.model.EarningDeductionType;
import com.example.payroll_service.model.EmployeeSalaryComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeSalaryComponentRepository extends JpaRepository<EmployeeSalaryComponent, UUID> {

    List<EmployeeSalaryComponent> findByTenantId(UUID tenantId);

    Optional<EmployeeSalaryComponent> findByTenantIdAndId(UUID tenantId, UUID id);

    List<EmployeeSalaryComponent> findByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    List<EmployeeSalaryComponent> findByTenantIdAndType(UUID tenantId, EarningDeductionType type);

    List<EmployeeSalaryComponent> findByTenantIdAndIsActive(UUID tenantId, Boolean isActive);
}
