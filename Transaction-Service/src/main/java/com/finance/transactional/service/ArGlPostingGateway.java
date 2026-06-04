package com.finance.transactional.service;

import com.finance.transactional.dto.event.ArSalesInvoiceApprovedEvent;
import com.finance.transactional.dto.event.ArSalesInvoiceGlPostResult;
import com.finance.transactional.model.ar.SalesInvoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArGlPostingGateway {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.custom.transactional-events-exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.custom.sales-invoice-approved-queue}")
    private String salesInvoiceApprovedRoutingKey;

    @Value("${ap.invoice-gl.reply-timeout-ms:45000}")
    private long replyTimeoutMs;

    public ArSalesInvoiceGlPostResult postSalesInvoiceAccrualAndWait(SalesInvoice invoice) {
        rabbitTemplate.setReplyTimeout(replyTimeoutMs);
        ArSalesInvoiceApprovedEvent event = toEvent(invoice);
        log.info("Requesting GL accrual for AR invoice {} via RabbitMQ", invoice.getInvoiceNumber());

        ArSalesInvoiceGlPostResult reply = rabbitTemplate.convertSendAndReceiveAsType(
                exchange,
                salesInvoiceApprovedRoutingKey,
                event,
                new ParameterizedTypeReference<ArSalesInvoiceGlPostResult>() {});

        if (reply == null) {
            return ArSalesInvoiceGlPostResult.failure(
                    "General Ledger posting timed out. Invoice was not approved. Check Core-Finance and RabbitMQ.");
        }
        return reply;
    }

    public static ArSalesInvoiceApprovedEvent toEvent(SalesInvoice invoice) {
        return ArSalesInvoiceApprovedEvent.builder()
                .id(invoice.getId())
                .tenantId(invoice.getTenantId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceDate(invoice.getInvoiceDate())
                .totalAmount(invoice.getTotalAmount())
                .build();
    }
}
