package com.example.payroll_service.service;

import com.example.payroll_service.dto.request.SalaryComponentRequest;
import com.example.payroll_service.dto.response.SalaryComponentResponse;
import com.example.payroll_service.dto.eventDto.PayrollEventDto;
import com.example.payroll_service.exception.ResourceNotFoundException;
import com.example.payroll_service.event.PayrollEventProducer;
import com.example.payroll_service.mapper.EmployeeSalaryComponentMapper;
import com.example.payroll_service.model.EarningDeductionType;
import com.example.payroll_service.model.EmployeeSalaryComponent;
import com.example.payroll_service.repository.EarningDeductionTypeRepository;
import com.example.payroll_service.repository.EmployeeSalaryComponentRepository;
import com.example.payroll_service.utility.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalaryComponentService {

    private final EmployeeSalaryComponentRepository employeeSalaryComponentRepository;
    private final EarningDeductionTypeRepository earningDeductionTypeRepository;
    private final EmployeeSalaryComponentMapper employeeSalaryComponentMapper;
    private final ValidationUtil validationUtil;
    private final PayrollEventProducer payrollEventProducer;

    @Transactional
    public SalaryComponentResponse assignComponentToEmployee(UUID tenantId, SalaryComponentRequest request, String username) {
        validationUtil.validateTenantAccess(tenantId);
        
        EarningDeductionType type = earningDeductionTypeRepository.findByTenantIdAndId(tenantId, request.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("EarningDeductionType", "id", request.getTypeId()));

        EmployeeSalaryComponent component = employeeSalaryComponentMapper.mapToEntity(request, tenantId);
        component.setType(type);
        component.setCreatedBy(username);
        component.setCreatedByUsername(username);
        component = employeeSalaryComponentRepository.save(component);

        // Send RabbitMQ event for salary component creation
        PayrollEventDto event = PayrollEventDto.builder()
                .id(component.getId())
                .tenantId(component.getTenantId())
                .eventType("SALARY_COMPONENT_CREATED")
                .employeeId(component.getEmployeeId())
                .amount(component.getAmount())
                .status(component.getIsActive() ? "ACTIVE" : "INACTIVE")
                .createdAt(component.getCreatedAt())
                .typeId(component.getType().getId())
                .build();

        payrollEventProducer.sendSalaryComponentEvent(event);

        log.info("Salary component assigned with id: {} for employee: {}", component.getId(), component.getEmployeeId());
        return employeeSalaryComponentMapper.mapToDto(component);
    }

    @Transactional
    public SalaryComponentResponse updateSalaryComponent(UUID tenantId, UUID id, SalaryComponentRequest request, String username) {
        validationUtil.validateTenantAccess(tenantId);
        EmployeeSalaryComponent component = employeeSalaryComponentRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeSalaryComponent", "id", id));

        EarningDeductionType type = earningDeductionTypeRepository.findByTenantIdAndId(tenantId, request.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("EarningDeductionType", "id", request.getTypeId()));

        component = employeeSalaryComponentMapper.updateEntity(component, request);
        component.setType(type);
        component.setUpdatedBy(username);
        component.setUpdatedByUsername(username);
        component = employeeSalaryComponentRepository.save(component);

        log.info("Salary component updated with id: {}", id);
        return employeeSalaryComponentMapper.mapToDto(component);
    }

    @Transactional
    public void deleteSalaryComponent(UUID tenantId, UUID id) {
        validationUtil.validateTenantAccess(tenantId);
        EmployeeSalaryComponent component = employeeSalaryComponentRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeSalaryComponent", "id", id));

        employeeSalaryComponentRepository.delete(component);

        log.info("Salary component deleted with id: {}", id);
    }

    @Transactional
    public SalaryComponentResponse activateComponent(UUID tenantId, UUID id, String username) {
        validationUtil.validateTenantAccess(tenantId);
        EmployeeSalaryComponent component = employeeSalaryComponentRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeSalaryComponent", "id", id));

        component.setIsActive(true);
        component.setUpdatedBy(username);
        component.setUpdatedByUsername(username);
        component = employeeSalaryComponentRepository.save(component);

        log.info("Salary component activated with id: {}", id);
        return employeeSalaryComponentMapper.mapToDto(component);
    }

    @Transactional
    public SalaryComponentResponse deactivateComponent(UUID tenantId, UUID id, String username) {
        validationUtil.validateTenantAccess(tenantId);
        EmployeeSalaryComponent component = employeeSalaryComponentRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeSalaryComponent", "id", id));

        component.setIsActive(false);
        component.setUpdatedBy(username);
        component.setUpdatedByUsername(username);
        component = employeeSalaryComponentRepository.save(component);

        log.info("Salary component deactivated with id: {}", id);
        return employeeSalaryComponentMapper.mapToDto(component);
    }

    public SalaryComponentResponse getSalaryComponentById(UUID tenantId, UUID id) {
        validationUtil.validateTenantAccess(tenantId);
        EmployeeSalaryComponent component = employeeSalaryComponentRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeSalaryComponent", "id", id));
        return employeeSalaryComponentMapper.mapToDto(component);
    }

    public List<SalaryComponentResponse> getAllSalaryComponents(UUID tenantId) {
        validationUtil.validateTenantAccess(tenantId);
        List<EmployeeSalaryComponent> components = employeeSalaryComponentRepository.findByTenantId(tenantId);
        return components.stream()
                .map(employeeSalaryComponentMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<SalaryComponentResponse> getEmployeeComponents(UUID tenantId, UUID employeeId) {
        validationUtil.validateTenantAccess(tenantId);
        List<EmployeeSalaryComponent> components = employeeSalaryComponentRepository.findByTenantIdAndEmployeeId(tenantId, employeeId);
        return components.stream()
                .map(employeeSalaryComponentMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<SalaryComponentResponse> getActiveEmployeeComponents(UUID tenantId, UUID employeeId) {
        validationUtil.validateTenantAccess(tenantId);
        List<EmployeeSalaryComponent> components = employeeSalaryComponentRepository.findByTenantIdAndEmployeeId(tenantId, employeeId);
        return components.stream()
                .filter(component -> Boolean.TRUE.equals(component.getIsActive()))
                .map(employeeSalaryComponentMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<SalaryComponentResponse> getComponentsByType(UUID tenantId, UUID typeId) {
        validationUtil.validateTenantAccess(tenantId);
        EarningDeductionType type = earningDeductionTypeRepository.findByTenantIdAndId(tenantId, typeId)
                .orElseThrow(() -> new ResourceNotFoundException("EarningDeductionType", "id", typeId));
        List<EmployeeSalaryComponent> components = employeeSalaryComponentRepository.findByTenantIdAndType(tenantId, type);
        return components.stream()
                .map(employeeSalaryComponentMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<SalaryComponentResponse> getComponentsByIsActive(UUID tenantId, Boolean isActive) {
        validationUtil.validateTenantAccess(tenantId);
        List<EmployeeSalaryComponent> components = employeeSalaryComponentRepository.findByTenantIdAndIsActive(tenantId, isActive);
        return components.stream()
                .map(employeeSalaryComponentMapper::mapToDto)
                .collect(Collectors.toList());
    }
}
