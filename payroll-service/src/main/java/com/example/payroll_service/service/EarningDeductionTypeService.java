package com.example.payroll_service.service;

import com.example.payroll_service.dto.request.EarningDeductionTypeRequest;
import com.example.payroll_service.dto.response.EarningDeductionTypeResponse;
import com.example.payroll_service.dto.eventDto.PayrollEventDto;
import com.example.payroll_service.enums.CalculationMethod;
import com.example.payroll_service.enums.EarningDeductionCategory;
import com.example.payroll_service.exception.ResourceExistsException;
import com.example.payroll_service.exception.ResourceNotFoundException;
import com.example.payroll_service.event.PayrollEventProducer;
import com.example.payroll_service.mapper.EarningDeductionTypeMapper;
import com.example.payroll_service.model.EarningDeductionType;
import com.example.payroll_service.repository.EarningDeductionTypeRepository;
import com.example.payroll_service.utility.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EarningDeductionTypeService {

    private final EarningDeductionTypeRepository earningDeductionTypeRepository;
    private final EarningDeductionTypeMapper earningDeductionTypeMapper;
    private final ValidationUtil validationUtil;
    private final PayrollEventProducer payrollEventProducer;

    @Transactional
    public EarningDeductionTypeResponse createType(UUID tenantId, EarningDeductionTypeRequest request, String username) {
        validationUtil.validateTenantAccess(tenantId);

        if (earningDeductionTypeRepository.existsByTenantIdAndTypeName(tenantId, request.getTypeName())) {
            throw new ResourceExistsException("EarningDeductionType", "typeName", request.getTypeName());
        }

        EarningDeductionType type = earningDeductionTypeMapper.mapToEntity(request, tenantId);
        type.setCreatedBy(username);
        type.setCreatedByUsername(username);
        type = earningDeductionTypeRepository.save(type);

        // Send RabbitMQ event for earning/deduction type creation
        boolean isActive = type.getStartDate() != null && 
                          (type.getEndDate() == null || type.getEndDate().isAfter(LocalDate.now()));
        
        PayrollEventDto event = PayrollEventDto.builder()
                .id(type.getId())
                .tenantId(type.getTenantId())
                .eventType("EARNING_DEDUCTION_TYPE_CREATED")
                .status(isActive ? "ACTIVE" : "INACTIVE")
                .createdAt(type.getCreatedAt())
                .typeId(type.getId())
                .build();

        payrollEventProducer.sendEarningDeductionTypeEvent(event);

        log.info("Earning/Deduction type created with id: {} for tenant: {}", type.getId(), tenantId);
        return earningDeductionTypeMapper.mapToDto(type);
    }

    @Transactional
    public EarningDeductionTypeResponse updateType(UUID tenantId, UUID id, EarningDeductionTypeRequest request, String username) {
        validationUtil.validateTenantAccess(tenantId);
        EarningDeductionType type = validationUtil.getEarningTypeById(tenantId, id);

        if (earningDeductionTypeRepository.existsByTenantIdAndTypeNameAndIdNot(tenantId, request.getTypeName(), id)) {
            throw new ResourceExistsException("EarningDeductionType", "typeName", request.getTypeName());
        }

        type = earningDeductionTypeMapper.updateEntity(type, request);
        type.setUpdatedBy(username);
        type.setUpdatedByUsername(username);
        type = earningDeductionTypeRepository.save(type);

        log.info("Earning/Deduction type updated with id: {}", id);
        return earningDeductionTypeMapper.mapToDto(type);
    }

    @Transactional
    public void deleteType(UUID tenantId, UUID id) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        EarningDeductionType type = validationUtil.getEarningTypeById(tenantId, id);

        earningDeductionTypeRepository.delete(type);

        log.info("Earning/Deduction type deleted with id: {}", id);
    }

    public EarningDeductionTypeResponse getTypeById(UUID tenantId, UUID id) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        EarningDeductionType type = validationUtil.getEarningTypeById(tenantId, id);
        return earningDeductionTypeMapper.mapToDto(type);
    }

    public List<EarningDeductionTypeResponse> getAllTypes(UUID tenantId) {
        validationUtil.validateTenantAccess(tenantId);
        List<EarningDeductionType> types = earningDeductionTypeRepository.findByTenantId(tenantId);
        return types.stream()
                .map(earningDeductionTypeMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<EarningDeductionTypeResponse> getTypesByCategory(UUID tenantId, EarningDeductionCategory category) {
        validationUtil.validateTenantAccess(tenantId);
        List<EarningDeductionType> types = earningDeductionTypeRepository.findByTenantIdAndCategory(tenantId, category);
        return types.stream()
                .map(earningDeductionTypeMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<EarningDeductionTypeResponse> getEarningTypes(UUID tenantId) {
        validationUtil.validateTenantAccess(tenantId);
        List<EarningDeductionType> types = earningDeductionTypeRepository.findByTenantId(tenantId);
        return types.stream()
                .filter(type -> Boolean.TRUE.equals(type.getIsEarning()))
                .map(earningDeductionTypeMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<EarningDeductionTypeResponse> getDeductionTypes(UUID tenantId) {
        validationUtil.validateTenantAccess(tenantId);
        List<EarningDeductionType> types = earningDeductionTypeRepository.findByTenantId(tenantId);
        return types.stream()
                .filter(type -> Boolean.FALSE.equals(type.getIsEarning()))
                .map(earningDeductionTypeMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<EarningDeductionTypeResponse> getTypesByCalculationMethod(UUID tenantId, CalculationMethod method) {
        validationUtil.validateTenantAccess(tenantId);
        List<EarningDeductionType> types = earningDeductionTypeRepository.findByTenantId(tenantId);
        return types.stream()
                .filter(type -> type.getCalculationMethod() == method)
                .map(earningDeductionTypeMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<EarningDeductionTypeResponse> getOvertimeEligibleTypes(UUID tenantId) {
        validationUtil.validateTenantAccess(tenantId);
        List<EarningDeductionType> types = earningDeductionTypeRepository.findByTenantId(tenantId);
        return types.stream()
                .filter(type -> Boolean.TRUE.equals(type.getOvertimeEligible()))
                .map(earningDeductionTypeMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<EarningDeductionTypeResponse> searchByTypeName(UUID tenantId, String keyword) {
        validationUtil.validateTenantAccess(tenantId);
        List<EarningDeductionType> types = earningDeductionTypeRepository.searchByTypeName(tenantId, keyword);
        return types.stream()
                .map(earningDeductionTypeMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<EarningDeductionTypeResponse> getActiveTypes(UUID tenantId, LocalDate currentDate) {
        validationUtil.validateTenantAccess(tenantId);
        List<EarningDeductionType> types = earningDeductionTypeRepository.findByTenantId(tenantId);
        LocalDate effectiveDate = currentDate != null ? currentDate : LocalDate.now();
        
        return types.stream()
                .filter(type -> {
                    boolean afterStart = type.getStartDate() == null || !effectiveDate.isBefore(type.getStartDate());
                    boolean beforeEnd = type.getEndDate() == null || !effectiveDate.isAfter(type.getEndDate());
                    return afterStart && beforeEnd;
                })
                .map(earningDeductionTypeMapper::mapToDto)
                .collect(Collectors.toList());
    }
}
