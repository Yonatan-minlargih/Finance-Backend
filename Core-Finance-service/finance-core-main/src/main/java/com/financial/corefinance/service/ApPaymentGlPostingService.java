package com.financial.corefinance.service;

import com.financial.corefinance.domain.entity.Account;
import com.financial.corefinance.domain.entity.AccountingPeriod;
import com.financial.corefinance.domain.entity.JournalHeader;
import com.financial.corefinance.dto.event.ApPaymentGlPostResult;
import com.financial.corefinance.dto.request.IntegrationJournalRequest;
import com.financial.corefinance.integration.IntegrationAccountKeys;
import com.financial.corefinance.repository.AccountingPeriodRepository;
import com.financial.corefinance.repository.JournalHeaderRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApPaymentGlPostingService {

    private static final String REFERENCE_TYPE = "AP_PAYMENT";

    private final IntegrationJournalService integrationJournalService;
    private final IntegrationGlAccountService integrationGlAccountService;
    private final AccountingPeriodRepository accountingPeriodRepository;
    private final JournalHeaderRepository journalHeaderRepository;

    @Value("${integration.contract-version:v1}")
    private String contractVersion;

    public ApPaymentGlPostResult postPayment(
            UUID tenantId,
            UUID paymentId,
            String paymentNumber,
            LocalDate paymentDate,
            BigDecimal amount) {
        if (tenantId == null || paymentId == null) {
            return ApPaymentGlPostResult.failure("Invalid payment event payload");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ApPaymentGlPostResult.failure("Payment amount must be greater than zero");
        }

        String tenantIdStr = tenantId.toString();
        try {
            journalHeaderRepository
                    .findFirstByTenantIdAndReferenceTypeAndReferenceIdAndStatus(
                            tenantIdStr, REFERENCE_TYPE, paymentId, JournalHeader.JournalStatus.POSTED)
                    .ifPresent(h -> {
                        throw new IllegalStateException("Payment already posted to GL: " + h.getJournalNumber());
                    });

            LocalDate effectiveDate = paymentDate != null ? paymentDate : LocalDate.now();
            AccountingPeriod period = accountingPeriodRepository
                    .findPeriodForDate(tenantIdStr, effectiveDate)
                    .filter(p -> Boolean.TRUE.equals(p.getIsOpen()) && !Boolean.TRUE.equals(p.getIsClosed()))
                    .orElse(null);
            if (period == null) {
                return ApPaymentGlPostResult.failure(
                        "No open accounting period for payment date " + effectiveDate);
            }

            integrationGlAccountService.seedIntegrationChart(tenantIdStr);
            Account apAccount = integrationGlAccountService.resolveByKey(tenantIdStr, IntegrationAccountKeys.AP_PAYABLE);
            Account bankAccount = integrationGlAccountService.resolveByKey(tenantIdStr, IntegrationAccountKeys.AP_BANK);

            List<IntegrationJournalRequest.JournalLineRequest> lines = new ArrayList<>();
            lines.add(line(apAccount.getId(), amount, BigDecimal.ZERO, "AP Payment settlement: " + paymentNumber));
            lines.add(line(bankAccount.getId(), BigDecimal.ZERO, amount, "AP Payment bank: " + paymentNumber));

            IntegrationJournalRequest request = new IntegrationJournalRequest();
            request.setContractVersion(contractVersion);
            request.setEventId(paymentId.toString());
            request.setIdempotencyKey("AP_PAYMENT:" + paymentId);
            request.setTenantId(tenantIdStr);
            request.setCorrelationId(paymentId.toString());
            request.setJournalDate(effectiveDate);
            request.setAccountingPeriodId(period.getId());
            request.setSourceSystem("TRANSACTIONAL_SERVICE");
            request.setReferenceNumber(paymentNumber);
            request.setReferenceType(REFERENCE_TYPE);
            request.setReferenceId(paymentId);
            request.setDescription("AP Payment posted — " + paymentNumber);
            request.setNarration(request.getDescription());
            request.setJournalLines(lines);

            JournalHeader posted = integrationJournalService.postJournal(request);
            log.info("Posted AP payment {} to GL as {}", paymentNumber, posted.getJournalNumber());
            return ApPaymentGlPostResult.success(posted.getId(), posted.getJournalNumber(), period.getId());
        } catch (Exception ex) {
            log.error("Failed to post AP payment {} to GL", paymentNumber, ex);
            return ApPaymentGlPostResult.failure(ex.getMessage());
        }
    }

    private static IntegrationJournalRequest.JournalLineRequest line(
            UUID accountId, BigDecimal debit, BigDecimal credit, String description) {
        IntegrationJournalRequest.JournalLineRequest line = new IntegrationJournalRequest.JournalLineRequest();
        line.setAccountId(accountId);
        line.setDebitAmount(debit);
        line.setCreditAmount(credit);
        line.setDescription(description);
        line.setCurrencyCode("ETB");
        line.setExchangeRate(BigDecimal.ONE);
        return line;
    }
}
