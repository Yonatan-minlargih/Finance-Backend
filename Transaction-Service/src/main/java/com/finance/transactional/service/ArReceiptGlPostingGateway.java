package com.finance.transactional.service;

import com.finance.transactional.dto.event.ArReceiptGlPostResult;
import com.finance.transactional.dto.event.ArReceiptPostedEvent;
import com.finance.transactional.model.ar.Receipt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArReceiptGlPostingGateway {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.custom.transactional-events-exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.custom.receipt-posted-queue}")
    private String receiptPostedRoutingKey;

    @Value("${ap.invoice-gl.reply-timeout-ms:45000}")
    private long replyTimeoutMs;

    public ArReceiptGlPostResult postReceiptAndWait(Receipt receipt) {
        rabbitTemplate.setReplyTimeout(replyTimeoutMs);
        ArReceiptPostedEvent event = ArReceiptPostedEvent.builder()
                .id(receipt.getId())
                .tenantId(receipt.getTenantId())
                .receiptNumber(receipt.getReceiptNumber())
                .receiptDate(receipt.getReceiptDate())
                .amount(receipt.getAmount())
                .build();

        log.info("Requesting GL post for AR receipt {} via RabbitMQ", receipt.getReceiptNumber());
        ArReceiptGlPostResult reply = rabbitTemplate.convertSendAndReceiveAsType(
                exchange,
                receiptPostedRoutingKey,
                event,
                new ParameterizedTypeReference<ArReceiptGlPostResult>() {});

        if (reply == null) {
            return ArReceiptGlPostResult.failure(
                    "General Ledger posting timed out. Receipt was not posted. Check Core-Finance and RabbitMQ.");
        }
        return reply;
    }
}
