package com.financial.corefinance.service;

import com.financial.corefinance.domain.entity.Account;
import com.financial.corefinance.domain.entity.AccountingPeriod;
import com.financial.corefinance.domain.entity.JournalHeader;
import com.financial.corefinance.dto.event.ApInvoiceApprovedEvent;
import com.financial.corefinance.dto.event.ApInvoiceGlPostResult;
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
public class ApInvoiceGlPostingService {

    private static final String REFERENCE_TYPE = "AP_INVOICE";
    private final IntegrationJournalService integrationJournalService;
    private final IntegrationGlAccountService integrationGlAccountService;
    private final AccountingPeriodRepository accountingPeriodRepository;
    private final JournalHeaderRepository journalHeaderRepository;

    @Value("${integration.contract-version:v1}")
    private String contractVersion;

    public ApInvoiceGlPostResult postInvoiceAccrual(ApInvoiceApprovedEvent event) {
        if (event == null || event.getId() == null || event.getTenantId() == null) {
            return ApInvoiceGlPostResult.failure("Invalid invoice approval event payload");
        }

        String tenantId = event.getTenantId().toString();
        UUID invoiceId = event.getId();

        try {
            journalHeaderRepository
                    .findFirstByTenantIdAndReferenceTypeAndReferenceIdAndStatus(
                            tenantId,
                            REFERENCE_TYPE,
                            invoiceId,
                            JournalHeader.JournalStatus.POSTED)
                    .ifPresent(existing -> {
                        throw new AlreadyPostedException(existing);
                    });

            LocalDate journalDate = event.getInvoiceDate() != null ? event.getInvoiceDate() : LocalDate.now();
            AccountingPeriod period = accountingPeriodRepository
                    .findPeriodForDate(tenantId, journalDate)
                    .filter(p -> Boolean.TRUE.equals(p.getIsOpen()) && !Boolean.TRUE.equals(p.getIsClosed()))
                    .orElse(null);

            if (period == null) {
                return ApInvoiceGlPostResult.failure(
                        "No open accounting period found for invoice date " + journalDate
                                + ". Close or open periods before approving.");
            }

            integrationGlAccountService.seedIntegrationChart(tenantId);
            Account apAccount = integrationGlAccountService.resolveByKey(tenantId, IntegrationAccountKeys.AP_PAYABLE);
            List<IntegrationJournalRequest.JournalLineRequest> lineRequests = new ArrayList<>();
            BigDecimal creditTotal = BigDecimal.ZERO;
            String txnCurrency = event.getCurrency() != null ? event.getCurrency() : "ETB";
            BigDecimal exchangeRate = event.getExchangeRate() != null ? event.getExchangeRate() : BigDecimal.ONE;
            boolean foreignTxn = txnCurrency != null
                    && !txnCurrency.equalsIgnoreCase("ETB")
                    && exchangeRate != null
                    && exchangeRate.compareTo(BigDecimal.ONE) != 0;

            if (event.getLines() != null && !event.getLines().isEmpty()) {
                for (ApInvoiceApprovedEvent.ApInvoiceLineEvent line : event.getLines()) {
                    if (line == null) {
                        continue;
                    }
                    BigDecimal lineAmt = line.getLineAmount() != null ? line.getLineAmount() : BigDecimal.ZERO;
                    if (lineAmt.compareTo(BigDecimal.ZERO) == 0) {
                        continue;
                    }
                    Account expenseAccount = resolveLineExpenseAccount(tenantId, line);
                    lineRequests.add(buildLine(
                            expenseAccount.getId(),
                            lineAmt,
                            BigDecimal.ZERO,
                            line.getDescription() != null ? line.getDescription()
                                    : "AP Invoice expense " + event.getInvoiceNumber(),
                            txnCurrency,
                            exchangeRate,
                            foreignTxn));
                    creditTotal = creditTotal.add(lineAmt);
                }
            }

            BigDecimal taxAmount = event.getTaxAmount() != null ? event.getTaxAmount() : BigDecimal.ZERO;

            if (creditTotal.compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal headerTotal = event.getTotalAmount() != null ? event.getTotalAmount() : BigDecimal.ZERO;
                if (headerTotal.compareTo(BigDecimal.ZERO) <= 0) {
                    return ApInvoiceGlPostResult.failure("Invoice amount must be greater than zero to post to GL");
                }
                BigDecimal expenseBase = headerTotal.subtract(taxAmount);
                if (expenseBase.compareTo(BigDecimal.ZERO) <= 0) {
                    return ApInvoiceGlPostResult.failure(
                            "Invoice subtotal must be greater than zero when VAT is present");
                }
                Account expenseAccount =
                        integrationGlAccountService.resolveByKey(tenantId, IntegrationAccountKeys.AP_EXPENSE_DEFAULT);
                lineRequests.add(buildLine(
                        expenseAccount.getId(),
                        expenseBase,
                        BigDecimal.ZERO,
                        "AP Invoice expense: " + event.getInvoiceNumber(),
                        txnCurrency,
                        exchangeRate,
                        foreignTxn));
                creditTotal = expenseBase;
            }

            if (taxAmount.compareTo(BigDecimal.ZERO) > 0) {
                Account vatInputAccount =
                        integrationGlAccountService.resolveByKey(tenantId, IntegrationAccountKeys.AP_VAT_INPUT);
                lineRequests.add(buildLine(
                        vatInputAccount.getId(),
                        taxAmount,
                        BigDecimal.ZERO,
                        "VAT Input: " + event.getInvoiceNumber(),
                        txnCurrency,
                        exchangeRate,
                        foreignTxn));
            }

            BigDecimal payableTotal = creditTotal.add(taxAmount);
            lineRequests.add(buildLine(
                    apAccount.getId(),
                    BigDecimal.ZERO,
                    payableTotal,
                    "AP Invoice payable: " + event.getInvoiceNumber(),
                    txnCurrency,
                    exchangeRate,
                    foreignTxn));

            String typeLabel = event.getInvoiceType() != null
                    ? event.getInvoiceType().replace('_', ' ')
                    : "Invoice";

            IntegrationJournalRequest request = new IntegrationJournalRequest();
            request.setContractVersion(contractVersion);
            request.setEventId(invoiceId.toString());
            request.setIdempotencyKey("AP_INVOICE:" + invoiceId);
            request.setTenantId(tenantId);
            request.setCorrelationId(invoiceId.toString());
            request.setJournalDate(journalDate);
            request.setAccountingPeriodId(period.getId());
            request.setSourceSystem("TRANSACTIONAL_SERVICE");
            request.setReferenceNumber(event.getInvoiceNumber());
            request.setReferenceType(REFERENCE_TYPE);
            request.setReferenceId(invoiceId);
            request.setDescription("AP " + typeLabel + " approved — " + event.getInvoiceNumber());
            request.setNarration("Vendor accrual from Transaction Service");
            request.setJournalLines(lineRequests);

            JournalHeader posted = integrationJournalService.postJournal(request);
            if (posted == null || posted.getId() == null) {
                return ApInvoiceGlPostResult.failure("General Ledger posting did not return a journal");
            }

            UUID fiscalYearId = period.getFiscalYearId();
            log.info("Posted AP invoice {} to GL as journal {}", event.getInvoiceNumber(), posted.getJournalNumber());

            return ApInvoiceGlPostResult.success(
                    posted.getId(),
                    posted.getJournalNumber(),
                    period.getId(),
                    fiscalYearId);

        } catch (AlreadyPostedException ex) {
            JournalHeader existing = ex.getJournal();
            AccountingPeriod period = accountingPeriodRepository.findById(existing.getAccountingPeriodId()).orElse(null);
            return ApInvoiceGlPostResult.success(
                    existing.getId(),
                    existing.getJournalNumber(),
                    existing.getAccountingPeriodId(),
                    period != null ? period.getFiscalYearId() : null);
        } catch (Exception ex) {
            log.error("Failed to post AP invoice {} to GL", event.getInvoiceNumber(), ex);
            return ApInvoiceGlPostResult.failure(
                    ex.getMessage() != null ? ex.getMessage() : "General Ledger posting failed");
        }
    }

    private Account resolveLineExpenseAccount(String tenantId, ApInvoiceApprovedEvent.ApInvoiceLineEvent line) {
        if (line.getAccountId() != null && !line.getAccountId().isBlank()) {
            String raw = line.getAccountId().trim();
            try {
                UUID.fromString(raw);
                return integrationGlAccountService.resolveByIdOrCode(tenantId, raw);
            } catch (IllegalArgumentException ignored) {
                return integrationGlAccountService.resolveByCode(tenantId, raw);
            }
        }
        return integrationGlAccountService.resolveByKey(tenantId, IntegrationAccountKeys.AP_EXPENSE_DEFAULT);
    }

    private IntegrationJournalRequest.JournalLineRequest buildLine(
            UUID accountId,
            BigDecimal debit,
            BigDecimal credit,
            String description,
            String txnCurrency,
            BigDecimal exchangeRate,
            boolean foreignTxn) {
        IntegrationJournalRequest.JournalLineRequest line = new IntegrationJournalRequest.JournalLineRequest();
        line.setAccountId(accountId);
        line.setDebitAmount(debit);
        line.setCreditAmount(credit);
        line.setDescription(description);
        line.setCurrencyCode(txnCurrency != null ? txnCurrency : "ETB");
        line.setExchangeRate(exchangeRate != null ? exchangeRate : BigDecimal.ONE);
        if (foreignTxn && exchangeRate != null && exchangeRate.compareTo(BigDecimal.ZERO) > 0) {
            if (debit != null && debit.compareTo(BigDecimal.ZERO) > 0) {
                line.setForeignDebitAmount(debit.divide(exchangeRate, 4, java.math.RoundingMode.HALF_UP));
            }
            if (credit != null && credit.compareTo(BigDecimal.ZERO) > 0) {
                line.setForeignCreditAmount(credit.divide(exchangeRate, 4, java.math.RoundingMode.HALF_UP));
            }
        }
        return line;
    }

    private static class AlreadyPostedException extends RuntimeException {
        private final JournalHeader journal;

        AlreadyPostedException(JournalHeader journal) {
            super("Invoice already posted");
            this.journal = journal;
        }

        JournalHeader getJournal() {
            return journal;
        }
    }
}
