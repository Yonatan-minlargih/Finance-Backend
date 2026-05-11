package com.example.payroll_service.event;

import com.example.payroll_service.dto.eventDto.PayrollEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayrollEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.payroll}")
    private String payrollExchange;

    @Value("${rabbitmq.routing-key.payroll.created}")
    private String payrollCreatedRoutingKey;

    @Value("${rabbitmq.routing-key.payroll.processed}")
    private String payrollProcessedRoutingKey;

    @Value("${rabbitmq.routing-key.payroll.approved}")
    private String payrollApprovedRoutingKey;

    @Value("${rabbitmq.routing-key.payroll.cancelled}")
    private String payrollCancelledRoutingKey;

    public void sendPayrollCreatedEvent(PayrollEventDto event) {
        try {
            rabbitTemplate.convertAndSend(payrollExchange, payrollCreatedRoutingKey, event);
            log.info("Payroll created event sent for payroll run id: {}", event.getId());
        } catch (Exception e) {
            log.error("Failed to send payroll created event for payroll run id: {}", event.getId(), e);
        }
    }

    public void sendPayrollProcessedEvent(PayrollEventDto event) {
        try {
            rabbitTemplate.convertAndSend(payrollExchange, payrollProcessedRoutingKey, event);
            log.info("Payroll processed event sent for payroll run id: {}", event.getId());
        } catch (Exception e) {
            log.error("Failed to send payroll processed event for payroll run id: {}", event.getId(), e);
        }
    }

    public void sendPayrollApprovedEvent(PayrollEventDto event) {
        try {
            rabbitTemplate.convertAndSend(payrollExchange, payrollApprovedRoutingKey, event);
            log.info("Payroll approved event sent for payroll run id: {}", event.getId());
        } catch (Exception e) {
            log.error("Failed to send payroll approved event for payroll run id: {}", event.getId(), e);
        }
    }

    public void sendPayrollCancelledEvent(PayrollEventDto event) {
        try {
            rabbitTemplate.convertAndSend(payrollExchange, payrollCancelledRoutingKey, event);
            log.info("Payroll cancelled event sent for payroll run id: {}", event.getId());
        } catch (Exception e) {
            log.error("Failed to send payroll cancelled event for payroll run id: {}", event.getId(), e);
        }
    }

    @Value("${rabbitmq.routing-key.loan.payment}")
    private String loanPaymentRoutingKey;

    @Value("${rabbitmq.routing-key.salary.component}")
    private String salaryComponentRoutingKey;

    public void sendLoanPaymentEvent(PayrollEventDto event) {
        try {
            rabbitTemplate.convertAndSend(payrollExchange, loanPaymentRoutingKey, event);
            log.info("Loan payment event sent for payment id: {}", event.getId());
        } catch (Exception e) {
            log.error("Failed to send loan payment event for payment id: {}", event.getId(), e);
        }
    }

    public void sendSalaryComponentEvent(PayrollEventDto event) {
        try {
            rabbitTemplate.convertAndSend(payrollExchange, salaryComponentRoutingKey, event);
            log.info("Salary component event sent for component id: {}", event.getId());
        } catch (Exception e) {
            log.error("Failed to send salary component event for component id: {}", event.getId(), e);
        }
    }

    public void sendEarningDeductionTypeEvent(PayrollEventDto event) {
        try {
            rabbitTemplate.convertAndSend(payrollExchange, salaryComponentRoutingKey, event);
            log.info("Earning/Deduction type event sent for type id: {}", event.getId());
        } catch (Exception e) {
            log.error("Failed to send earning/deduction type event for type id: {}", event.getId(), e);
        }
    }
}
