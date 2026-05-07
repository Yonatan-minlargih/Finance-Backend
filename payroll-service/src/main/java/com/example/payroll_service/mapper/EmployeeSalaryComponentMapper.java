package com.example.payroll_service.mapper;

import com.example.payroll_service.dto.eventDto.PayrollEventDto;
import com.example.payroll_service.dto.request.SalaryComponentRequest;
import com.example.payroll_service.dto.response.SalaryComponentResponse;
import com.example.payroll_service.model.EmployeeSalaryComponent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmployeeSalaryComponentMapper {

    public EmployeeSalaryComponent mapToEntity(SalaryComponentRequest request, UUID tenantId) {
        EmployeeSalaryComponent component = new EmployeeSalaryComponent();
        component.setEmployeeId(request.getEmployeeId());
        component.setAmount(request.getAmount());
        component.setIsActive(true);
        component.setTenantId(tenantId);
        return component;
    }

    public EmployeeSalaryComponent updateEntity(EmployeeSalaryComponent component, SalaryComponentRequest request) {
        component.setEmployeeId(request.getEmployeeId());
        component.setAmount(request.getAmount());
        return component;
    }

    public SalaryComponentResponse mapToDto(EmployeeSalaryComponent component) {
        return SalaryComponentResponse.builder()
                .id(component.getId())
                .tenantId(component.getTenantId())
                .employeeId(component.getEmployeeId())
                .amount(component.getAmount())
                .isActive(component.getIsActive())
                .typeId(component.getType() != null ? component.getType().getId() : null)
                .createdAt(component.getCreatedAt())
                .updatedAt(component.getUpdatedAt())
                .createdBy(component.getCreatedBy())
                .updatedBy(component.getUpdatedBy())
                .build();
    }

    public PayrollEventDto mapToEvent(EmployeeSalaryComponent component) {
        return PayrollEventDto.builder()
                .id(component.getId())
                .tenantId(component.getTenantId())
                .eventType("SALARY_COMPONENT")
                .employeeId(component.getEmployeeId())
                .createdAt(component.getCreatedAt())
                .updatedAt(component.getUpdatedAt())
                .build();
    }
}
