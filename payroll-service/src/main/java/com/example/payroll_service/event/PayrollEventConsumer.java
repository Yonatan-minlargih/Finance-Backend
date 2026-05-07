package com.example.payroll_service.event;

import com.example.payroll_service.dto.eventDto.PayrollEventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Payroll Event Consumer
 * 
 * Consumes payroll events from RabbitMQ queues and processes them.
 * Uses direct @RabbitListener annotations for simple event processing.
 */
@Component
@Slf4j
public class PayrollEventConsumer {

    /**
     * Handles payroll run created events
     */
    @RabbitListener(queues = "${rabbitmq.queue.payroll-created}")
    public void handlePayrollRunCreated(PayrollEventDto event) {
        try {
            log.info("Received payroll run created event: id={}, tenantId={}, runDate={}", 
                    event.getId(), event.getTenantId(), event.getRunDate());
            
            // Process payroll run created event
            processPayrollRunCreated(event);
            
        } catch (Exception e) {
            log.error("Failed to process payroll run created event: id={}", event.getId(), e);
        }
    }

    /**
     * Handles payroll run processed events
     */
    @RabbitListener(queues = "${rabbitmq.queue.payroll-processed}")
    public void handlePayrollRunProcessed(PayrollEventDto event) {
        try {
            log.info("Received payroll run processed event: id={}, tenantId={}, totalGross={}", 
                    event.getId(), event.getTenantId(), event.getTotalGross());
            
            // Process payroll run processed event
            processPayrollRunProcessed(event);
            
        } catch (Exception e) {
            log.error("Failed to process payroll run processed event: id={}", event.getId(), e);
        }
    }

    /**
     * Handles payroll run approved events
     */
    @RabbitListener(queues = "${rabbitmq.queue.payroll-approved}")
    public void handlePayrollRunApproved(PayrollEventDto event) {
        try {
            log.info("Received payroll run approved event: id={}, tenantId={}, approvedAt={}", 
                    event.getId(), event.getTenantId(), event.getApprovedAt());
            
            // Process payroll run approved event
            processPayrollRunApproved(event);
            
        } catch (Exception e) {
            log.error("Failed to process payroll run approved event: id={}", event.getId(), e);
        }
    }

    /**
     * Handles payroll run cancelled events
     */
    @RabbitListener(queues = "${rabbitmq.queue.payroll-cancelled}")
    public void handlePayrollRunCancelled(PayrollEventDto event) {
        try {
            log.info("Received payroll run cancelled event: id={}, tenantId={}, cancelledAt={}", 
                    event.getId(), event.getTenantId(), event.getCancelledAt());
            
            // Process payroll run cancelled event
            processPayrollRunCancelled(event);
            
        } catch (Exception e) {
            log.error("Failed to process payroll run cancelled event: id={}", event.getId(), e);
        }
    }

    /**
     * Handles loan payment events
     */
    @RabbitListener(queues = "${rabbitmq.queue.loan-payment}")
    public void handleLoanPayment(PayrollEventDto event) {
        try {
            log.info("Received loan payment event: id={}, tenantId={}, amount={}", 
                    event.getId(), event.getTenantId(), event.getAmount());
            
            // Process loan payment event
            processLoanPayment(event);
            
        } catch (Exception e) {
            log.error("Failed to process loan payment event: id={}", event.getId(), e);
        }
    }

    /**
     * Handles salary component events
     */
    @RabbitListener(queues = "${rabbitmq.queue.salary-component}")
    public void handleSalaryComponent(PayrollEventDto event) {
        try {
            log.info("Received salary component event: id={}, tenantId={}, amount={}", 
                    event.getId(), event.getTenantId(), event.getAmount());
            
            // Process salary component event
            processSalaryComponent(event);
            
        } catch (Exception e) {
            log.error("Failed to process salary component event: id={}", event.getId(), e);
        }
    }

    // Business logic methods for processing events
    
    private void processPayrollRunCreated(PayrollEventDto event) {
        // TODO: Implement business logic for payroll run created
        // Examples:
        // - Send notifications to HR department
        // - Update analytics dashboards
        // - Trigger downstream processes
        log.info("Processing payroll run created business logic for tenant: {}", event.getTenantId());
    }

    private void processPayrollRunProcessed(PayrollEventDto event) {
        // TODO: Implement business logic for payroll run processed
        // Examples:
        // - Generate payroll reports
        // - Update financial records
        // - Send payment instructions to bank
        log.info("Processing payroll run processed business logic for tenant: {}", event.getTenantId());
    }

    private void processPayrollRunApproved(PayrollEventDto event) {
        // TODO: Implement business logic for payroll run approved
        // Examples:
        // - Finalize payroll calculations
        // - Archive payroll data
        // - Send approval notifications
        log.info("Processing payroll run approved business logic for tenant: {}", event.getTenantId());
    }

    private void processPayrollRunCancelled(PayrollEventDto event) {
        // TODO: Implement business logic for payroll run cancelled
        // Examples:
        // - Rollback transactions
        // - Send cancellation notifications
        // - Update audit logs
        log.info("Processing payroll run cancelled business logic for tenant: {}", event.getTenantId());
    }

    private void processLoanPayment(PayrollEventDto event) {
        // TODO: Implement business logic for loan payment
        // Examples:
        // - Update loan balances
        // - Generate payment receipts
        // - Send payment confirmations
        log.info("Processing loan payment business logic for tenant: {}", event.getTenantId());
    }

    private void processSalaryComponent(PayrollEventDto event) {
        // TODO: Implement business logic for salary component
        // Examples:
        // - Update salary calculations
        // - Recalculate payroll totals
        // - Update employee records
        log.info("Processing salary component business logic for tenant: {}", event.getTenantId());
    }
}
