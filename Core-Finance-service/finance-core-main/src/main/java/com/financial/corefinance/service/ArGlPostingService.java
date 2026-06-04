package com.financial.corefinance.service;

import com.financial.corefinance.domain.entity.Account;
import com.financial.corefinance.domain.entity.AccountingPeriod;
import com.financial.corefinance.domain.entity.JournalHeader;
import com.financial.corefinance.dto.event.ArReceiptGlPostResult;
import com.financial.corefinance.dto.event.ArSalesInvoiceApprovedEvent;
import com.financial.corefinance.dto.event.ArSalesInvoiceGlPostResult;
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
public class ArGlPostingService {

    private final IntegrationJournalService integrationJournalService;
    private final IntegrationGlAccountService integrationGlAccountService;
    private final AccountingPeriodRepository accountingPeriodRepository;
    private final JournalHeaderRepository journalHeaderRepository;

    @Value("${integration.contract-version:v1}")
    private String contractVersion;

    public ArSalesInvoiceGlPostResult postSalesInvoiceAccrual(ArSalesInvoiceApprovedEvent event) {
        if (event == null || event.getId() == null || event.getTenantId() == null) {
            return ArSalesInvoiceGlPostResult.failure("Invalid sales invoice approval event");
        }
        try {
            JournalHeader posted = postSalesInvoice(
                    event.getTenantId(),
                    event.getId(),
                    event.getInvoiceNumber(),
                    event.getInvoiceDate(),
                    event.getTotalAmount());
            return ArSalesInvoiceGlPostResult.success(
                    posted.getId(), posted.getJournalNumber(), posted.getAccountingPeriodId());
        } catch (Exception ex) {
            log.error("AR sales invoice GL post failed for {}", event.getInvoiceNumber(), ex);
            return ArSalesInvoiceGlPostResult.failure(ex.getMessage());
        }
    }

    public JournalHeader postSalesInvoice(
            UUID tenantId,
            UUID invoiceId,
            String invoiceNumber,
            LocalDate invoiceDate,
            BigDecimal totalAmount) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Sales invoice amount must be greater than zero");
        }
        String tenantIdStr = tenantId.toString();
        journalHeaderRepository
                .findFirstByTenantIdAndReferenceTypeAndReferenceIdAndStatus(
                        tenantIdStr, "AR_SALES_INVOICE", invoiceId, JournalHeader.JournalStatus.POSTED)
                .ifPresent(h -> {
                    throw new IllegalStateException("Sales invoice already posted to GL: " + h.getJournalNumber());
                });

        AccountingPeriod period = resolveOpenPeriod(tenantIdStr, invoiceDate);
        integrationGlAccountService.seedIntegrationChart(tenantIdStr);
        Account arAccount = integrationGlAccountService.resolveByKey(tenantIdStr, IntegrationAccountKeys.AR_RECEIVABLE);
        Account revenueAccount = integrationGlAccountService.resolveByKey(tenantIdStr, IntegrationAccountKeys.AR_REVENUE);

        List<IntegrationJournalRequest.JournalLineRequest> lines = new ArrayList<>();
        lines.add(line(arAccount.getId(), totalAmount, BigDecimal.ZERO, "AR Receivable: " + invoiceNumber));
        lines.add(line(revenueAccount.getId(), BigDecimal.ZERO, totalAmount, "Revenue: " + invoiceNumber));

        IntegrationJournalRequest request = baseRequest(
                tenantIdStr,
                invoiceId,
                "AR_INVOICE:" + invoiceId,
                invoiceDate,
                period.getId(),
                invoiceNumber,
                "AR_SALES_INVOICE",
                invoiceId,
                "AR Sales invoice — " + invoiceNumber,
                lines);

        JournalHeader posted = integrationJournalService.postJournal(request);
        log.info("Posted AR sales invoice {} to GL as {}", invoiceNumber, posted.getJournalNumber());
        return posted;
    }

    public ArReceiptGlPostResult postReceiptAccrual(
            UUID tenantId, UUID receiptId, String receiptNumber, LocalDate receiptDate, BigDecimal amount) {
        try {
            JournalHeader posted = postReceipt(tenantId, receiptId, receiptNumber, receiptDate, amount);
            return ArReceiptGlPostResult.success(posted.getId(), posted.getJournalNumber());
        } catch (Exception ex) {
            log.error("AR receipt GL post failed for {}", receiptNumber, ex);
            return ArReceiptGlPostResult.failure(ex.getMessage());
        }
    }

    public JournalHeader postReceipt(
            UUID tenantId,
            UUID receiptId,
            String receiptNumber,
            LocalDate receiptDate,
            BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Receipt amount must be greater than zero");
        }
        String tenantIdStr = tenantId.toString();
        AccountingPeriod period = resolveOpenPeriod(tenantIdStr, receiptDate);
        integrationGlAccountService.seedIntegrationChart(tenantIdStr);
        Account bankAccount = integrationGlAccountService.resolveByKey(tenantIdStr, IntegrationAccountKeys.AP_BANK);
        Account arAccount = integrationGlAccountService.resolveByKey(tenantIdStr, IntegrationAccountKeys.AR_RECEIVABLE);

        List<IntegrationJournalRequest.JournalLineRequest> lines = new ArrayList<>();
        lines.add(line(bankAccount.getId(), amount, BigDecimal.ZERO, "Cash receipt: " + receiptNumber));
        lines.add(line(arAccount.getId(), BigDecimal.ZERO, amount, "AR collection: " + receiptNumber));

        IntegrationJournalRequest request = baseRequest(
                tenantIdStr,
                receiptId,
                "AR_RECEIPT:" + receiptId,
                receiptDate,
                period.getId(),
                receiptNumber,
                "AR_RECEIPT",
                receiptId,
                "AR Receipt posted — " + receiptNumber,
                lines);

        JournalHeader posted = integrationJournalService.postJournal(request);
        log.info("Posted AR receipt {} to GL as {}", receiptNumber, posted.getJournalNumber());
        return posted;
    }

    public JournalHeader postWriteOff(
            UUID tenantId,
            UUID invoiceId,
            String invoiceNumber,
            LocalDate adjustmentDate,
            BigDecimal amount,
            String reason) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Write-off amount must be greater than zero");
        }
        String tenantIdStr = tenantId.toString();
        AccountingPeriod period = resolveOpenPeriod(tenantIdStr, adjustmentDate);
        integrationGlAccountService.seedIntegrationChart(tenantIdStr);
        Account arAccount = integrationGlAccountService.resolveByKey(tenantIdStr, IntegrationAccountKeys.AR_RECEIVABLE);
        Account expenseAccount =
                integrationGlAccountService.resolveByKey(tenantIdStr, IntegrationAccountKeys.AR_BAD_DEBT_EXPENSE);

        List<IntegrationJournalRequest.JournalLineRequest> lines = new ArrayList<>();
        lines.add(line(expenseAccount.getId(), amount, BigDecimal.ZERO, "Bad debt write-off: " + invoiceNumber));
        lines.add(line(arAccount.getId(), BigDecimal.ZERO, amount, "Write-off AR: " + invoiceNumber));

        IntegrationJournalRequest request = baseRequest(
                tenantIdStr,
                invoiceId,
                "AR_WRITEOFF:" + invoiceId + ":" + adjustmentDate,
                adjustmentDate,
                period.getId(),
                invoiceNumber,
                "AR_WRITEOFF",
                invoiceId,
                "AR Write-off — " + invoiceNumber + (reason != null ? " (" + reason + ")" : ""),
                lines);

        return integrationJournalService.postJournal(request);
    }

    public JournalHeader postInterest(
            UUID tenantId,
            UUID invoiceId,
            String invoiceNumber,
            LocalDate assessmentDate,
            BigDecimal interestAmount,
            String reason) {
        if (interestAmount == null || interestAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Interest amount must be greater than zero");
        }
        String tenantIdStr = tenantId.toString();
        AccountingPeriod period = resolveOpenPeriod(tenantIdStr, assessmentDate);
        integrationGlAccountService.seedIntegrationChart(tenantIdStr);
        Account arAccount = integrationGlAccountService.resolveByKey(tenantIdStr, IntegrationAccountKeys.AR_RECEIVABLE);
        Account interestIncome =
                integrationGlAccountService.resolveByKey(tenantIdStr, IntegrationAccountKeys.AR_INTEREST_INCOME);

        List<IntegrationJournalRequest.JournalLineRequest> lines = new ArrayList<>();
        lines.add(line(arAccount.getId(), interestAmount, BigDecimal.ZERO, "Interest receivable: " + invoiceNumber));
        lines.add(
                line(interestIncome.getId(), BigDecimal.ZERO, interestAmount, "Interest income: " + invoiceNumber));

        IntegrationJournalRequest request = baseRequest(
                tenantIdStr,
                invoiceId,
                "AR_INTEREST:" + invoiceId + ":" + assessmentDate,
                assessmentDate,
                period.getId(),
                invoiceNumber,
                "AR_INTEREST",
                invoiceId,
                "AR Interest — " + invoiceNumber + (reason != null ? " (" + reason + ")" : ""),
                lines);

        return integrationJournalService.postJournal(request);
    }

    private AccountingPeriod resolveOpenPeriod(String tenantId, LocalDate date) {
        LocalDate effective = date != null ? date : LocalDate.now();
        AccountingPeriod period = accountingPeriodRepository
                .findPeriodForDate(tenantId, effective)
                .orElseThrow(() -> new IllegalStateException("No accounting period for date " + effective));
        if (!Boolean.TRUE.equals(period.getIsOpen()) || Boolean.TRUE.equals(period.getIsClosed())) {
            throw new IllegalStateException("Accounting period is closed for date " + effective);
        }
        return period;
    }

    private IntegrationJournalRequest baseRequest(
            String tenantId,
            UUID eventId,
            String idempotencyKey,
            LocalDate journalDate,
            UUID periodId,
            String referenceNumber,
            String referenceType,
            UUID referenceId,
            String description,
            List<IntegrationJournalRequest.JournalLineRequest> lines) {
        IntegrationJournalRequest request = new IntegrationJournalRequest();
        request.setContractVersion(contractVersion);
        request.setEventId(eventId.toString());
        request.setIdempotencyKey(idempotencyKey);
        request.setTenantId(tenantId);
        request.setCorrelationId(eventId.toString());
        request.setJournalDate(journalDate);
        request.setAccountingPeriodId(periodId);
        request.setSourceSystem("TRANSACTIONAL_SERVICE");
        request.setReferenceNumber(referenceNumber);
        request.setReferenceType(referenceType);
        request.setReferenceId(referenceId);
        request.setDescription(description);
        request.setNarration(description);
        request.setJournalLines(lines);
        return request;
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
