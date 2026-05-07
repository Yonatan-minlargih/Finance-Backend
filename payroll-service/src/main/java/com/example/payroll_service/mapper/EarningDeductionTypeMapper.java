package com.example.payroll_service.mapper;

import com.example.payroll_service.dto.eventDto.PayrollEventDto;
import com.example.payroll_service.dto.request.EarningDeductionTypeRequest;
import com.example.payroll_service.dto.response.EarningDeductionTypeResponse;
import com.example.payroll_service.model.EarningDeductionType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EarningDeductionTypeMapper {

    public EarningDeductionType mapToEntity(EarningDeductionTypeRequest request, UUID tenantId) {
        EarningDeductionType type = new EarningDeductionType();
        type.setTypeName(request.getTypeName());
        type.setIsEarning(request.getIsEarning());
        type.setIsPercentage(request.getIsPercentage());
        type.setDefaultValue(request.getDefaultValue());
        type.setGlAccountId(request.getGlAccountId());
        type.setCategory(request.getCategory());
        type.setCalculationMethod(request.getCalculationMethod());
        type.setOvertimeEligible(request.getOvertimeEligible());
        type.setStartDate(request.getStartDate());
        type.setEndDate(request.getEndDate());
        type.setTenantId(tenantId);
        return type;
    }

    public EarningDeductionType updateEntity(EarningDeductionType type, EarningDeductionTypeRequest request) {
        type.setTypeName(request.getTypeName());
        type.setIsEarning(request.getIsEarning());
        type.setIsPercentage(request.getIsPercentage());
        type.setDefaultValue(request.getDefaultValue());
        type.setGlAccountId(request.getGlAccountId());
        type.setCategory(request.getCategory());
        type.setCalculationMethod(request.getCalculationMethod());
        type.setOvertimeEligible(request.getOvertimeEligible());
        type.setStartDate(request.getStartDate());
        type.setEndDate(request.getEndDate());
        return type;
    }

    public EarningDeductionTypeResponse mapToDto(EarningDeductionType type) {
        return EarningDeductionTypeResponse.builder()
                .id(type.getId())
                .tenantId(type.getTenantId())
                .typeName(type.getTypeName())
                .isEarning(type.getIsEarning())
                .isPercentage(type.getIsPercentage())
                .defaultValue(type.getDefaultValue())
                .glAccountId(type.getGlAccountId())
                .category(type.getCategory())
                .calculationMethod(type.getCalculationMethod())
                .overtimeEligible(type.getOvertimeEligible())
                .startDate(type.getStartDate())
                .endDate(type.getEndDate())
                .createdAt(type.getCreatedAt())
                .updatedAt(type.getUpdatedAt())
                .createdBy(type.getCreatedBy())
                .updatedBy(type.getUpdatedBy())
                .build();
    }

    public PayrollEventDto mapToEvent(EarningDeductionType type) {
        return PayrollEventDto.builder()
                .id(type.getId())
                .tenantId(type.getTenantId())
                .eventType("EARNING_DEDUCTION_TYPE")
                .createdAt(type.getCreatedAt())
                .updatedAt(type.getUpdatedAt())
                .build();
    }
}
