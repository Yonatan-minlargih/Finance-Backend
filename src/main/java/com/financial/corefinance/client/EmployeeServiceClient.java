package com.financial.corefinance.client;

import com.financial.corefinance.dto.employeeDto.EmployeeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "employee-service", url = "${employee.service.url}")
public interface EmployeeServiceClient {

    @GetMapping("/api/v1/employees/{employeeId}")
    EmployeeDto getEmployeeById(@PathVariable UUID employeeId);

    @GetMapping("/api/v1/employees/by-tenant/{tenantId}")
    List<EmployeeDto> getEmployeesByTenant(@PathVariable String tenantId);

    @GetMapping("/api/v1/employees/department/{departmentId}")
    List<EmployeeDto> getEmployeesByDepartment(@PathVariable UUID departmentId);

    @GetMapping("/api/v1/employees/active")
    List<EmployeeDto> getActiveEmployees();

    @PostMapping("/api/v1/employees/{employeeId}/validate")
    boolean validateEmployee(@PathVariable UUID employeeId);

    @GetMapping("/api/v1/employees/{employeeId}/cost-center")
    String getEmployeeCostCenter(@PathVariable UUID employeeId);

    @GetMapping("/api/v1/employees/{employeeId}/department")
    String getEmployeeDepartment(@PathVariable UUID employeeId);

    @GetMapping("/api/v1/employees/{employeeId}/manager")
    String getEmployeeManager(@PathVariable UUID employeeId);
}
