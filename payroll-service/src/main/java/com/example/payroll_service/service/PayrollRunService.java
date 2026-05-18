package com.example.payroll_service.service;

import com.example.payroll_service.dto.request.PayrollRunRequest;
import com.example.payroll_service.dto.response.PayrollRunResponse;
import com.example.payroll_service.enums.PayrollStatus;
import com.example.payroll_service.exception.InvalidPayrollStateException;
import com.example.payroll_service.exception.PayrollProcessingException;
import com.example.payroll_service.exception.ResourceExistsException;
import com.example.payroll_service.exception.ResourceNotFoundException;
import com.example.payroll_service.event.PayrollEventProducer;
import com.example.payroll_service.mapper.PayrollRunMapper;
import com.example.payroll_service.model.PayrollRun;
import com.example.payroll_service.repository.PayrollRunRepository;
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
public class PayrollRunService {

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollRunMapper payrollRunMapper;
    private final ValidationUtil validationUtil;
    private final PayrollEventProducer payrollEventProducer;
    private final com.example.payroll_service.repository.PayrollDetailRepository payrollDetailRepository;

    @Transactional
    public PayrollRunResponse createPayrollRun(UUID tenantId, PayrollRunRequest request, String username) {
        validationUtil.validateTenantAccess(tenantId);

        // Validate period exists and belongs to tenant
        validationUtil.getPeriodById(tenantId, request.getPeriodId());

        if (payrollRunRepository.existsByTenantIdAndPeriodId(tenantId, request.getPeriodId())) {
            throw new ResourceExistsException("PayrollRun", "periodId", request.getPeriodId());
        }

        PayrollRun payrollRun = payrollRunMapper.mapToEntity(request, tenantId);
        payrollRun.setCreatedBy(username);
        payrollRun.setCreatedByUsername(username);
        payrollRun = payrollRunRepository.save(payrollRun);

        payrollEventProducer.sendPayrollCreatedEvent(payrollRunMapper.mapToEvent(payrollRun));

        log.info("Payroll run created with id: {} for tenant: {}", payrollRun.getId(), tenantId);
        return payrollRunMapper.mapToDto(payrollRun);
    }

    @Transactional
    public PayrollRunResponse updatePayrollRun(UUID tenantId, UUID id, PayrollRunRequest request, String username) {
        validationUtil.validateTenantAccess(tenantId);
        PayrollRun payrollRun = validationUtil.getPayrollRunById(tenantId, id);

        // Validate period exists and belongs to tenant
        validationUtil.getPeriodById(tenantId, request.getPeriodId());

        if (payrollRunRepository.existsByTenantIdAndPeriodIdAndIdNot(tenantId, request.getPeriodId(), id)) {
            throw new ResourceExistsException("PayrollRun", "periodId", request.getPeriodId());
        }

        validationUtil.validatePayrollStateForProcessing(payrollRun);

        payrollRun = payrollRunMapper.updateEntity(payrollRun, request);
        payrollRun.setUpdatedBy(username);
        payrollRun.setUpdatedByUsername(username);
        payrollRun = payrollRunRepository.save(payrollRun);

        payrollEventProducer.sendPayrollProcessedEvent(payrollRunMapper.mapToEvent(payrollRun));

        log.info("Payroll run updated with id: {}", id);
        return payrollRunMapper.mapToDto(payrollRun);
    }

    @Transactional
    public void deletePayrollRun(UUID tenantId, UUID id) {
        validationUtil.validateTenantAccess(tenantId);
        PayrollRun payrollRun = validationUtil.getPayrollRunById(tenantId, id);

        if (payrollRun.getStatus() == PayrollStatus.POSTED) {
            throw new InvalidPayrollStateException("Cannot delete posted payroll");
        }

        payrollRunRepository.delete(payrollRun);

        payrollEventProducer.sendPayrollCancelledEvent(payrollRunMapper.mapToEvent(payrollRun));

        log.info("Payroll run deleted with id: {}", id);
    }

    public PayrollRunResponse getPayrollRunById(UUID tenantId, UUID id) {
        validationUtil.validateTenantAccess(tenantId);
        PayrollRun payrollRun = validationUtil.getPayrollRunById(tenantId, id);
        return payrollRunMapper.mapToDto(payrollRun);
    }

    public List<PayrollRunResponse> getAllPayrollRuns(UUID tenantId) {
        validationUtil.validateTenantAccess(tenantId);
        List<PayrollRun> payrollRuns = payrollRunRepository.findByTenantId(tenantId);
        return payrollRuns.stream()
                .map(payrollRunMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<PayrollRunResponse> getPayrollRunsByStatus(UUID tenantId, PayrollStatus status) {
        validationUtil.validateTenantAccess(tenantId);
        List<PayrollRun> payrollRuns = payrollRunRepository.findByTenantIdAndStatus(tenantId, status);
        return payrollRuns.stream()
                .map(payrollRunMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<PayrollRunResponse> getPayrollRunsByDateRange(UUID tenantId, LocalDate startDate, LocalDate endDate) {
        validationUtil.validateTenantAccess(tenantId);
        List<PayrollRun> payrollRuns = payrollRunRepository.findByTenantIdAndRunDateBetween(tenantId, startDate, endDate);
        return payrollRuns.stream()
                .map(payrollRunMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<PayrollRunResponse> getPayrollRunsByPeriodId(UUID tenantId, UUID periodId) {
        validationUtil.validateTenantAccess(tenantId);
        // Validate period exists and belongs to tenant
        validationUtil.getPeriodById(tenantId, periodId);
        List<PayrollRun> payrollRuns = payrollRunRepository.findByTenantIdAndPeriodId(tenantId, periodId);
        return payrollRuns.stream()
                .map(payrollRunMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PayrollRunResponse processPayroll(UUID tenantId, UUID id, String username) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        PayrollRun payrollRun = validationUtil.getPayrollRunById(tenantId, id);

        validationUtil.validatePayrollStateForProcessing(payrollRun);

        payrollRun.setStatus(PayrollStatus.PROCESSING);
        payrollRun.setUpdatedBy(username);
        payrollRun.setUpdatedByUsername(username);
        payrollRun = payrollRunRepository.save(payrollRun);

        try {
            // Dynamically aggregate gross and net from the recorded payroll details
            List<com.example.payroll_service.model.PayrollDetail> details = 
                payrollDetailRepository.findByTenantIdAndPayrollRun(tenantId, payrollRun);

            java.math.BigDecimal totalGross = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalNet = java.math.BigDecimal.ZERO;

            if (details != null) {
                for (com.example.payroll_service.model.PayrollDetail detail : details) {
                    if (detail.getGrossSalary() != null) {
                        totalGross = totalGross.add(detail.getGrossSalary());
                    }
                    if (detail.getNetSalary() != null) {
                        totalNet = totalNet.add(detail.getNetSalary());
                    }
                }
            }

            payrollRun.setTotalGross(totalGross);
            payrollRun.setTotalNet(totalNet);
            payrollRun.setStatus(PayrollStatus.PROCESSED);
            payrollRun.setUpdatedBy(username);
            payrollRun.setUpdatedByUsername(username);
            payrollRun = payrollRunRepository.save(payrollRun);

            payrollEventProducer.sendPayrollProcessedEvent(payrollRunMapper.mapToEvent(payrollRun));

            log.info("Payroll processed with aggregated totals (Gross: {}, Net: {}) with id: {} for tenant: {}", 
                    totalGross, totalNet, id, tenantId);
        } catch (Exception e) {
            payrollRun.setStatus(PayrollStatus.CANCELLED);
            payrollRun.setUpdatedBy(username);
            payrollRun.setUpdatedByUsername(username);
            payrollRunRepository.save(payrollRun);

            throw new PayrollProcessingException("Failed to process payroll: " + e.getMessage(), e);
        }

        return payrollRunMapper.mapToDto(payrollRun);
    }

    @Transactional
    public PayrollRunResponse approvePayroll(UUID tenantId, UUID id, String username) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        PayrollRun payrollRun = validationUtil.getPayrollRunById(tenantId, id);

        validationUtil.validatePayrollStateForApproval(payrollRun);

        payrollRun.setStatus(PayrollStatus.APPROVED);
        payrollRun.setUpdatedBy(username);
        payrollRun.setUpdatedByUsername(username);
        payrollRun = payrollRunRepository.save(payrollRun);

        payrollEventProducer.sendPayrollApprovedEvent(payrollRunMapper.mapToEvent(payrollRun));

        log.info("Payroll approved with id: {}", id);
        return payrollRunMapper.mapToDto(payrollRun);
    }

    @Transactional
    public PayrollRunResponse postPayroll(UUID tenantId, UUID id, String username) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        PayrollRun payrollRun = validationUtil.getPayrollRunById(tenantId, id);

        validationUtil.validatePayrollStateForPosting(payrollRun);

        payrollRun.setStatus(PayrollStatus.POSTED);
        payrollRun.setUpdatedBy(username);
        payrollRun.setUpdatedByUsername(username);
        payrollRun = payrollRunRepository.save(payrollRun);

        payrollEventProducer.sendPayrollApprovedEvent(payrollRunMapper.mapToEvent(payrollRun));

        log.info("Payroll posted with id: {}", id);
        return payrollRunMapper.mapToDto(payrollRun);
    }

    @Transactional
    public PayrollRunResponse cancelPayroll(UUID tenantId, UUID id, String username) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        PayrollRun payrollRun = validationUtil.getPayrollRunById(tenantId, id);

        if (payrollRun.getStatus() == PayrollStatus.POSTED) {
            throw new InvalidPayrollStateException("Cannot cancel posted payroll");
        }

        payrollRun.setStatus(PayrollStatus.CANCELLED);
        payrollRun.setUpdatedBy(username);
        payrollRun.setUpdatedByUsername(username);
        payrollRun = payrollRunRepository.save(payrollRun);

        payrollEventProducer.sendPayrollCancelledEvent(payrollRunMapper.mapToEvent(payrollRun));

        log.info("Payroll cancelled with id: {}", id);
        return payrollRunMapper.mapToDto(payrollRun);
    }
}
