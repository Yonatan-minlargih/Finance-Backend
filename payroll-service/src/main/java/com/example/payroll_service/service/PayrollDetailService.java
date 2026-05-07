package com.example.payroll_service.service;

import com.example.payroll_service.dto.response.PayrollDetailResponse;
import com.example.payroll_service.exception.InvalidPayrollStateException;
import com.example.payroll_service.exception.ResourceNotFoundException;
import com.example.payroll_service.enums.PayrollStatus;
import com.example.payroll_service.event.PayrollEventProducer;
import com.example.payroll_service.mapper.PayrollDetailMapper;
import com.example.payroll_service.model.PayrollDetail;
import com.example.payroll_service.model.PayrollRun;
import com.example.payroll_service.repository.PayrollDetailRepository;
import com.example.payroll_service.utility.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollDetailService {

    private final PayrollDetailRepository payrollDetailRepository;
    private final PayrollDetailMapper payrollDetailMapper;
    private final ValidationUtil validationUtil;
    private final PayrollEventProducer payrollEventProducer;

    @Transactional
    public PayrollDetailResponse createPayrollDetail(UUID tenantId, UUID employeeId, BigDecimal basicSalary, 
                                                      BigDecimal overtime, BigDecimal bonus, UUID payrollRunId, String username) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        PayrollRun payrollRun = validationUtil.getPayrollRunById(tenantId, payrollRunId);

        validationUtil.validatePayrollStateForProcessing(payrollRun);

        PayrollDetail detail = new PayrollDetail();
        detail.setEmployeeId(employeeId);
        detail.setBasicSalary(basicSalary);
        detail.setOvertime(overtime);
        detail.setBonusAmount(bonus);
        detail.setPayrollRun(payrollRun);
        detail.setTenantId(tenantId);
        detail.setCreatedBy(username);
        detail.setCreatedByUsername(username);

        detail = calculateSalaryDetails(detail);
        detail = payrollDetailRepository.save(detail);

        payrollEventProducer.sendPayrollProcessedEvent(payrollDetailMapper.mapToEvent(detail));

        log.info("Payroll detail created with id: {} for employee: {}", detail.getId(), employeeId);
        return payrollDetailMapper.mapToDto(detail);
    }

    @Transactional
    public PayrollDetailResponse updatePayrollDetail(UUID tenantId, UUID id, BigDecimal basicSalary, 
                                                      BigDecimal overtime, BigDecimal bonus, String username) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        PayrollDetail detail = validationUtil.getPayrollDetailById(tenantId, id);

        detail.setBasicSalary(basicSalary);
        detail.setOvertime(overtime);
        detail.setBonusAmount(bonus);
        detail.setUpdatedBy(username);
        detail.setUpdatedByUsername(username);

        detail = calculateSalaryDetails(detail);
        detail = payrollDetailRepository.save(detail);

        payrollEventProducer.sendPayrollProcessedEvent(payrollDetailMapper.mapToEvent(detail));

        log.info("Payroll detail updated with id: {}", id);
        return payrollDetailMapper.mapToDto(detail);
    }

    @Transactional
    public void deletePayrollDetail(UUID tenantId, UUID id) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        PayrollDetail detail = validationUtil.getPayrollDetailById(tenantId, id);

        PayrollRun payrollRun = detail.getPayrollRun();
        if (payrollRun != null && payrollRun.getStatus() == PayrollStatus.POSTED) {
            throw new InvalidPayrollStateException("Cannot delete payroll detail for posted payroll");
        }

        payrollDetailRepository.delete(detail);

        payrollEventProducer.sendPayrollCancelledEvent(payrollDetailMapper.mapToEvent(detail));

        log.info("Payroll detail deleted with id: {}", id);
    }

    public PayrollDetailResponse getPayrollDetailById(UUID tenantId, UUID id) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        PayrollDetail detail = validationUtil.getPayrollDetailById(tenantId, id);
        return payrollDetailMapper.mapToDto(detail);
    }

    public List<PayrollDetailResponse> getAllPayrollDetails(UUID tenantId) {
        validationUtil.validateTenantAccess(tenantId);
        List<PayrollDetail> details = payrollDetailRepository.findByTenantId(tenantId);
        return details.stream()
                .map(payrollDetailMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<PayrollDetailResponse> getPayrollDetailsByPayrollRun(UUID tenantId, UUID payrollRunId) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        PayrollRun payrollRun = validationUtil.getPayrollRunById(tenantId, payrollRunId);
        List<PayrollDetail> details = payrollDetailRepository.findByTenantIdAndPayrollRun(tenantId, payrollRun);
        return details.stream()
                .map(payrollDetailMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<PayrollDetailResponse> getPayrollDetailsByEmployee(UUID tenantId, UUID employeeId) {
        validationUtil.validateTenantAccess(tenantId);
        List<PayrollDetail> details = payrollDetailRepository.findByTenantIdAndEmployeeId(tenantId, employeeId);
        return details.stream()
                .map(payrollDetailMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public List<PayrollDetailResponse> getPayrollDetailsByEmployeeAndPayrollRun(UUID tenantId, UUID payrollRunId, UUID employeeId) throws ResourceNotFoundException {
        validationUtil.validateTenantAccess(tenantId);
        PayrollRun payrollRun = validationUtil.getPayrollRunById(tenantId, payrollRunId);
        List<PayrollDetail> details = payrollDetailRepository.findByTenantIdAndPayrollRunAndEmployeeId(tenantId, payrollRun, employeeId);
        return details.stream()
                .map(payrollDetailMapper::mapToDto)
                .collect(Collectors.toList());
    }

    private PayrollDetail calculateSalaryDetails(PayrollDetail detail) {
        BigDecimal gross = detail.getBasicSalary() != null ? detail.getBasicSalary() : BigDecimal.ZERO;
        gross = gross.add(detail.getOvertime() != null ? detail.getOvertime() : BigDecimal.ZERO);
        gross = gross.add(detail.getBonusAmount() != null ? detail.getBonusAmount() : BigDecimal.ZERO);
        detail.setGrossSalary(gross);

        BigDecimal deductions = detail.getNetToGrossAmount() != null ? detail.getNetToGrossAmount() : BigDecimal.ZERO;
        detail.setTotalDeductions(deductions);

        BigDecimal net = gross.subtract(deductions);
        detail.setNetSalary(net);

        return detail;
    }
}
