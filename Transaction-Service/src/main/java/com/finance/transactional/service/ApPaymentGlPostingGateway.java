package com.finance.transactional.service;

import com.finance.transactional.dto.event.ApPaymentGlPostResult;
import com.finance.transactional.dto.event.ApPaymentPostedEvent;
import com.finance.transactional.model.ap.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApPaymentGlPostingGateway {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.custom.transactional-events-exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.custom.payment-posted-queue}")
    private String paymentPostedRoutingKey;

    @Value("${ap.invoice-gl.reply-timeout-ms:45000}")
    private long replyTimeoutMs;

    public ApPaymentGlPostResult postPaymentAndWait(Payment payment) {
        rabbitTemplate.setReplyTimeout(replyTimeoutMs);
        ApPaymentPostedEvent event = ApPaymentPostedEvent.builder()
                .id(payment.getId())
                .tenantId(payment.getTenantId())
                .paymentNumber(payment.getPaymentNumber())
                .paymentDate(payment.getPaymentDate())
                .amount(payment.getAmount())
                .build();

        log.info("Requesting GL post for AP payment {} via RabbitMQ", payment.getPaymentNumber());
        ApPaymentGlPostResult reply = rabbitTemplate.convertSendAndReceiveAsType(
                exchange,
                paymentPostedRoutingKey,
                event,
                new ParameterizedTypeReference<ApPaymentGlPostResult>() {});

        if (reply == null) {
            return ApPaymentGlPostResult.failure(
                    "General Ledger posting timed out. Payment was not recorded. Check Core-Finance and RabbitMQ.");
        }
        return reply;
    }
}
