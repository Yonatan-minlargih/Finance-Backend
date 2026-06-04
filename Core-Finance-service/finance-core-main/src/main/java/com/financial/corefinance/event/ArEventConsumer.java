package com.financial.corefinance.event;

import com.financial.corefinance.domain.base.TenantContext;
import com.financial.corefinance.dto.event.ArReceiptGlPostResult;
import com.financial.corefinance.dto.event.ArReceiptPostedEvent;
import com.financial.corefinance.dto.event.ArSalesInvoiceApprovedEvent;
import com.financial.corefinance.dto.event.ArSalesInvoiceGlPostResult;
import com.financial.corefinance.service.ArGlPostingService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArEventConsumer {

    private final ArGlPostingService arGlPostingService;

    /** Request-reply: approve sales invoice and post Dr AR / Cr Revenue. */
    @RabbitListener(
            bindings =
                    @QueueBinding(
                            value = @Queue(value = "${rabbitmq.salesInvoiceApprovedQueue}", durable = "true"),
                            exchange = @Exchange(value = "${rabbitmq.transactionalEventsExchange}", type = "direct"),
                            key = "${rabbitmq.salesInvoiceApprovedQueue}"))
    public ArSalesInvoiceGlPostResult handleSalesInvoiceApproved(@Payload ArSalesInvoiceApprovedEvent event) {
        log.info("AR sales invoice approved — posting accrual: {}", event != null ? event.getInvoiceNumber() : null);
        try {
            if (event != null && event.getTenantId() != null) {
                TenantContext.setCurrentTenant(event.getTenantId().toString());
            }
            return arGlPostingService.postSalesInvoiceAccrual(event);
        } finally {
            TenantContext.clear();
        }
    }

    /** Request-reply: post receipt Dr Cash / Cr AR after subledger application. */
    @RabbitListener(
            bindings =
                    @QueueBinding(
                            value = @Queue(value = "${rabbitmq.receiptPostedQueue}", durable = "true"),
                            exchange = @Exchange(value = "${rabbitmq.transactionalEventsExchange}", type = "direct"),
                            key = "${rabbitmq.receiptPostedQueue}"))
    public ArReceiptGlPostResult handleReceiptPosted(@Payload ArReceiptPostedEvent event) {
        log.info("AR receipt posted — posting to GL: {}", event != null ? event.getReceiptNumber() : null);
        try {
            if (event != null && event.getTenantId() != null) {
                TenantContext.setCurrentTenant(event.getTenantId().toString());
            }
            if (event == null || event.getId() == null || event.getTenantId() == null) {
                return ArReceiptGlPostResult.failure("Invalid receipt event");
            }
            return arGlPostingService.postReceiptAccrual(
                    event.getTenantId(),
                    event.getId(),
                    event.getReceiptNumber(),
                    event.getReceiptDate(),
                    event.getAmount());
        } finally {
            TenantContext.clear();
        }
    }

    @RabbitListener(
            bindings =
                    @QueueBinding(
                            value = @Queue(value = "${rabbitmq.arWriteOffQueue:ar-write-off-queue}", durable = "true"),
                            exchange = @Exchange(value = "${rabbitmq.transactionalEventsExchange}", type = "direct"),
                            key = "${rabbitmq.arWriteOffQueue:ar-write-off-queue}"))
    public void handleWriteOff(@Payload Map<String, Object> event) {
        log.info("AR write-off for invoice {}", event.get("invoiceNumber"));
        try {
            setTenant(uuid(event, "tenantId"));
            arGlPostingService.postWriteOff(
                    uuid(event, "tenantId"),
                    uuid(event, "invoiceId"),
                    string(event, "invoiceNumber"),
                    localDate(event, "adjustmentDate"),
                    decimal(event, "amount"),
                    string(event, "reason"));
        } catch (Exception e) {
            log.error("Failed to post AR write-off", e);
        } finally {
            clearTenant();
        }
    }

    @RabbitListener(
            bindings =
                    @QueueBinding(
                            value =
                                    @Queue(
                                            value = "${rabbitmq.arInterestQueue:ar-interest-queue}",
                                            durable = "true"),
                            exchange = @Exchange(value = "${rabbitmq.transactionalEventsExchange}", type = "direct"),
                            key = "${rabbitmq.arInterestQueue:ar-interest-queue}"))
    public void handleInterest(@Payload Map<String, Object> event) {
        log.info("AR interest on invoice {}", event.get("invoiceNumber"));
        try {
            setTenant(uuid(event, "tenantId"));
            arGlPostingService.postInterest(
                    uuid(event, "tenantId"),
                    uuid(event, "invoiceId"),
                    string(event, "invoiceNumber"),
                    localDate(event, "assessmentDate"),
                    decimal(event, "interestAmount"),
                    string(event, "reason"));
        } catch (Exception e) {
            log.error("Failed to post AR interest", e);
        } finally {
            clearTenant();
        }
    }

    private static void setTenant(UUID tenantId) {
        if (tenantId != null) {
            TenantContext.setCurrentTenant(tenantId.toString());
        }
    }

    private static void clearTenant() {
        TenantContext.clear();
    }

    private static UUID uuid(Map<String, Object> event, String key) {
        Object v = event.get(key);
        if (v == null) {
            throw new IllegalArgumentException("Missing event field: " + key);
        }
        return UUID.fromString(v.toString());
    }

    private static String string(Map<String, Object> event, String key) {
        Object v = event.get(key);
        return v != null ? v.toString() : null;
    }

    private static LocalDate localDate(Map<String, Object> event, String key) {
        Object v = event.get(key);
        if (v == null) {
            return LocalDate.now();
        }
        return LocalDate.parse(v.toString().substring(0, 10));
    }

    private static BigDecimal decimal(Map<String, Object> event, String key) {
        Object v = event.get(key);
        if (v == null) {
            throw new IllegalArgumentException("Missing event field: " + key);
        }
        return new BigDecimal(v.toString());
    }
}
