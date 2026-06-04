package com.finance.transactional.service;

import com.finance.transactional.dto.event.ApInvoiceApprovedEvent;
import com.finance.transactional.dto.event.ApInvoiceGlPostResult;
import com.finance.transactional.model.ap.Invoice;
import com.finance.transactional.model.ap.InvoiceLine;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceGlPostingGateway {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.custom.transactional-events-exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.custom.invoice-approved-queue}")
    private String invoiceApprovedRoutingKey;

    @Value("${ap.invoice-gl.reply-timeout-ms:45000}")
    private long replyTimeoutMs;

    public ApInvoiceGlPostResult postInvoiceAccrualAndWait(Invoice invoice) {
        rabbitTemplate.setReplyTimeout(replyTimeoutMs);
        ApInvoiceApprovedEvent event = toEvent(invoice);

        log.info("Requesting GL accrual post for AP invoice {} via RabbitMQ", invoice.getInvoiceNumber());
        ApInvoiceGlPostResult reply = rabbitTemplate.convertSendAndReceiveAsType(
                exchange,
                invoiceApprovedRoutingKey,
                event,
                new ParameterizedTypeReference<ApInvoiceGlPostResult>() {});

        if (reply == null) {
            log.error("No reply from Core-Finance for invoice {}", invoice.getId());
            return ApInvoiceGlPostResult.failure(
                    "General Ledger posting timed out. Invoice was not approved. Check Core-Finance and RabbitMQ.");
        }

        return reply;
    }

    public static ApInvoiceApprovedEvent toEvent(Invoice invoice) {
        List<ApInvoiceApprovedEvent.ApInvoiceLineEvent> lines = null;
        if (invoice.getLines() != null) {
            lines = invoice.getLines().stream().map(line -> ApInvoiceApprovedEvent.ApInvoiceLineEvent.builder()
                    .id(line.getId())
                    .description(line.getDescription())
                    .quantity(line.getQuantity())
                    .unitPrice(line.getUnitPrice())
                    .lineAmount(line.getLineAmount())
                    .accountId(line.getAccountId())
                    .build()).collect(Collectors.toList());
        }

        return ApInvoiceApprovedEvent.builder()
                .id(invoice.getId())
                .tenantId(invoice.getTenantId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .totalAmount(invoice.getTotalAmount())
                .taxAmount(invoice.getTaxAmount())
                .currency(invoice.getCurrency())
                .foreignTotalAmount(invoice.getForeignTotalAmount())
                .exchangeRate(invoice.getExchangeRate())
                .invoiceType(invoice.getInvoiceType() != null ? invoice.getInvoiceType().name() : null)
                .vendorId(invoice.getVendor() != null ? invoice.getVendor().getId() : null)
                .lines(lines)
                .build();
    }
}
