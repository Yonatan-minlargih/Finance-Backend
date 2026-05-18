package com.example.payroll_service.mapper;

import com.example.payroll_service.dto.eventDto.PayrollEventDto;
import com.example.payroll_service.dto.request.PayrollRunRequest;
import com.example.payroll_service.dto.response.PayrollRunResponse;
import com.example.payroll_service.model.PayrollRun;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PayrollRunMapper {

    public PayrollRun mapToEntity(PayrollRunRequest request, UUID tenantId) {
        PayrollRun payrollRun = new PayrollRun();
        payrollRun.setRunDate(request.getRunDate());
        payrollRun.setPeriodId(request.getPeriodId());
        payrollRun.setTotalGross(request.getTotalGross());
        payrollRun.setTotalNet(request.getTotalNet());
        payrollRun.setStatus(request.getStatus());
        payrollRun.setJournalId(request.getJournalId());
        payrollRun.setPeriodStartDate(request.getPeriodStartDate());
        payrollRun.setPeriodEndDate(request.getPeriodEndDate());
        payrollRun.setTenantId(tenantId);
        return payrollRun;
    }

    public PayrollRun updateEntity(PayrollRun payrollRun, PayrollRunRequest request) {
        payrollRun.setRunDate(request.getRunDate());
        payrollRun.setPeriodId(request.getPeriodId());
        payrollRun.setTotalGross(request.getTotalGross());
        payrollRun.setTotalNet(request.getTotalNet());
        payrollRun.setStatus(request.getStatus());
        payrollRun.setJournalId(request.getJournalId());
        payrollRun.setPeriodStartDate(request.getPeriodStartDate());
        payrollRun.setPeriodEndDate(request.getPeriodEndDate());
        return payrollRun;
    }

    public PayrollRunResponse mapToDto(PayrollRun payrollRun) {
        return PayrollRunResponse.builder()
                .id(payrollRun.getId())
                .tenantId(payrollRun.getTenantId())
                .runDate(payrollRun.getRunDate())
                .periodId(payrollRun.getPeriodId())
                .totalGross(payrollRun.getTotalGross())
                .totalNet(payrollRun.getTotalNet())
                .status(payrollRun.getStatus())
                .journalId(payrollRun.getJournalId())
                .periodStartDate(payrollRun.getPeriodStartDate())
                .periodEndDate(payrollRun.getPeriodEndDate())
                .createdAt(payrollRun.getCreatedAt())
                .updatedAt(payrollRun.getUpdatedAt())
                .createdBy(payrollRun.getCreatedBy())
                .updatedBy(payrollRun.getUpdatedBy())
                .build();
    }

    public PayrollEventDto mapToEvent(PayrollRun payrollRun) {
        return PayrollEventDto.builder()
                .id(payrollRun.getId())
                .tenantId(payrollRun.getTenantId())
                .eventType("PAYROLL_RUN")
                .runDate(payrollRun.getRunDate())
                .periodId(payrollRun.getPeriodId())
                .totalGross(payrollRun.getTotalGross())
                .totalNet(payrollRun.getTotalNet())
                .status(payrollRun.getStatus() != null ? payrollRun.getStatus().name() : null)
                .createdAt(payrollRun.getCreatedAt())
                .updatedAt(payrollRun.getUpdatedAt())
                .build();
    }
}
