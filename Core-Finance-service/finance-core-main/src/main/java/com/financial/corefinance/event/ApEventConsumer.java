package com.financial.corefinance.event;

import com.financial.corefinance.dto.event.ApInvoiceApprovedEvent;
import com.financial.corefinance.dto.event.ApInvoiceGlPostResult;
import com.financial.corefinance.dto.event.ApPaymentGlPostResult;
import com.financial.corefinance.dto.event.ApPaymentPostedEvent;
import com.financial.corefinance.service.ApInvoiceGlPostingService;
import com.financial.corefinance.service.ApPaymentGlPostingService;
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
public class ApEventConsumer {

    private final ApInvoiceGlPostingService apInvoiceGlPostingService;
    private final ApPaymentGlPostingService apPaymentGlPostingService;

    @RabbitListener(
            bindings =
                    @QueueBinding(
                            value = @Queue(value = "${rabbitmq.invoiceApprovedQueue}", durable = "true"),
                            exchange = @Exchange(value = "${rabbitmq.transactionalEventsExchange}", type = "direct"),
                            key = "${rabbitmq.invoiceApprovedQueue}"))
    public ApInvoiceGlPostResult handleInvoiceApproved(@Payload ApInvoiceApprovedEvent event) {
        log.info(
                "AP invoice approved — posting accrual to GL: id={}, number={}",
                event != null ? event.getId() : null,
                event != null ? event.getInvoiceNumber() : null);
        return apInvoiceGlPostingService.postInvoiceAccrual(event);
    }

    @RabbitListener(
            bindings =
                    @QueueBinding(
                            value = @Queue(value = "${rabbitmq.paymentPostedQueue}", durable = "true"),
                            exchange = @Exchange(value = "${rabbitmq.transactionalEventsExchange}", type = "direct"),
                            key = "${rabbitmq.paymentPostedQueue}"))
    public ApPaymentGlPostResult handlePaymentPosted(@Payload ApPaymentPostedEvent event) {
        log.info("AP payment posted — posting to GL: {}", event != null ? event.getPaymentNumber() : null);
        if (event == null || event.getId() == null || event.getTenantId() == null) {
            return ApPaymentGlPostResult.failure("Invalid payment event");
        }
        return apPaymentGlPostingService.postPayment(
                event.getTenantId(),
                event.getId(),
                event.getPaymentNumber(),
                event.getPaymentDate(),
                event.getAmount());
    }
}
