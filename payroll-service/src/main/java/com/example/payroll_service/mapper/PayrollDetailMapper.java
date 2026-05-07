package com.example.payroll_service.mapper;

import com.example.payroll_service.dto.eventDto.PayrollEventDto;
import com.example.payroll_service.dto.response.PayrollDetailResponse;
import com.example.payroll_service.model.PayrollDetail;
import org.springframework.stereotype.Component;

@Component
public class PayrollDetailMapper {

    public PayrollDetailResponse mapToDto(PayrollDetail payrollDetail) {
        return PayrollDetailResponse.builder()
                .id(payrollDetail.getId())
                .tenantId(payrollDetail.getTenantId())
                .employeeId(payrollDetail.getEmployeeId())
                .basicSalary(payrollDetail.getBasicSalary())
                .overtime(payrollDetail.getOvertime())
                .grossSalary(payrollDetail.getGrossSalary())
                .totalDeductions(payrollDetail.getTotalDeductions())
                .netSalary(payrollDetail.getNetSalary())
                .bonusAmount(payrollDetail.getBonusAmount())
                .netToGrossAmount(payrollDetail.getNetToGrossAmount())
                .journalId(payrollDetail.getJournalId())
                .payrollRunId(payrollDetail.getPayrollRun() != null ? payrollDetail.getPayrollRun().getId() : null)
                .createdAt(payrollDetail.getCreatedAt())
                .updatedAt(payrollDetail.getUpdatedAt())
                .createdBy(payrollDetail.getCreatedBy())
                .updatedBy(payrollDetail.getUpdatedBy())
                .build();
    }

    public PayrollEventDto mapToEvent(PayrollDetail payrollDetail) {
        return PayrollEventDto.builder()
                .id(payrollDetail.getId())
                .tenantId(payrollDetail.getTenantId())
                .eventType("PAYROLL_DETAIL")
                .employeeId(payrollDetail.getEmployeeId())
                .createdAt(payrollDetail.getCreatedAt())
                .updatedAt(payrollDetail.getUpdatedAt())
                .build();
    }
}
