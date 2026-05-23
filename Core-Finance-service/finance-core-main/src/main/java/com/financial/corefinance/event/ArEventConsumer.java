package com.financial.corefinance.event;

import com.financial.corefinance.domain.entity.Account;
import com.financial.corefinance.domain.entity.AccountingPeriod;
import com.financial.corefinance.domain.entity.JournalHeader;
import com.financial.corefinance.domain.entity.JournalLine;
import com.financial.corefinance.repository.AccountRepository;
import com.financial.corefinance.repository.AccountingPeriodRepository;
import com.financial.corefinance.repository.JournalHeaderRepository;
import com.financial.corefinance.repository.JournalLineRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArEventConsumer {

    private final JournalHeaderRepository journalHeaderRepository;
    private final JournalLineRepository journalLineRepository;
    private final AccountingPeriodRepository periodRepository;
    private final AccountRepository accountRepository;

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.salesInvoiceApprovedQueue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.transactionalEventsExchange}", type = "direct"),
            key = "${rabbitmq.salesInvoiceApprovedQueue}"
        )
    )
    public void handleSalesInvoiceApproved(SalesInvoiceEventDto event) {
        log.info("📥 Received AR Sales Invoice Approved event: {}", event.getInvoiceNumber());
        try {
            String tenantIdStr = event.getTenantId() != null ? event.getTenantId().toString() : "default";
            com.financial.corefinance.domain.base.TenantContext.setCurrentTenant(tenantIdStr);

            LocalDate invoiceDate = event.getInvoiceDate();
            if (invoiceDate == null) invoiceDate = LocalDate.now();

            AccountingPeriod period = periodRepository.findPeriodForDate(tenantIdStr, invoiceDate).orElse(null);
            if (period == null || !Boolean.TRUE.equals(period.getIsOpen())) {
                log.error("❌ No open accounting period found for date {}. Skipping sales invoice journal.", invoiceDate);
                return;
            }

            JournalHeader header = JournalHeader.builder()
                    .tenantId(tenantIdStr)
                    .journalNumber("AR-INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .journalDate(invoiceDate)
                    .accountingPeriodId(period.getId())
                    .journalType(JournalHeader.JournalType.SYSTEM)
                    .description("Auto-Journal: AR Sales Invoice Approved " + event.getInvoiceNumber())
                    .status(JournalHeader.JournalStatus.DRAFT)
                    .sourceSystem("TRANSACTION_SERVICE")
                    .referenceId(event.getId())
                    .referenceNumber(event.getInvoiceNumber())
                    .referenceType("AR_SALES_INVOICE")
                    .build();

            JournalHeader savedHeader = journalHeaderRepository.save(header);

            // Accounts Receivable (Debit)
            Account arAccount = resolveOrCreateAccount(tenantIdStr, "1200", "Accounts Receivable", Account.AccountType.ASSET);
            JournalLine arLine = JournalLine.builder()
                    .journalHeader(savedHeader)
                    .accountId(arAccount.getId())
                    .debitAmount(event.getTotalAmount())
                    .creditAmount(BigDecimal.ZERO)
                    .description("Receivable for " + event.getInvoiceNumber())
                    .build();
            journalLineRepository.save(arLine);

            // Revenue (Credit)
            Account revenueAccount = resolveOrCreateAccount(tenantIdStr, "4100", "Sales Revenue", Account.AccountType.REVENUE);
            JournalLine revenueLine = JournalLine.builder()
                    .journalHeader(savedHeader)
                    .accountId(revenueAccount.getId())
                    .debitAmount(BigDecimal.ZERO)
                    .creditAmount(event.getTotalAmount())
                    .description("Revenue for " + event.getInvoiceNumber())
                    .build();
            journalLineRepository.save(revenueLine);

            log.info("✅ AR Sales Invoice auto-journal created successfully: {}", savedHeader.getJournalNumber());
        } catch (Exception e) {
            log.error("❌ Failed to process AR Sales Invoice Approved event", e);
        } finally {
            com.financial.corefinance.domain.base.TenantContext.clear();
        }
    }

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.receiptPostedQueue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.transactionalEventsExchange}", type = "direct"),
            key = "${rabbitmq.receiptPostedQueue}"
        )
    )
    public void handleReceiptPosted(ReceiptEventDto event) {
        log.info("📥 Received AR Receipt Posted event: {}", event.getReceiptNumber());
        try {
            String tenantIdStr = event.getTenantId() != null ? event.getTenantId().toString() : "default";
            com.financial.corefinance.domain.base.TenantContext.setCurrentTenant(tenantIdStr);

            LocalDate receiptDate = event.getReceiptDate();
            if (receiptDate == null) receiptDate = LocalDate.now();

            AccountingPeriod period = periodRepository.findPeriodForDate(tenantIdStr, receiptDate).orElse(null);
            if (period == null || !Boolean.TRUE.equals(period.getIsOpen())) {
                log.error("❌ No open accounting period found for date {}. Skipping receipt journal.", receiptDate);
                return;
            }

            JournalHeader header = JournalHeader.builder()
                    .tenantId(tenantIdStr)
                    .journalNumber("AR-RCP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .journalDate(receiptDate)
                    .accountingPeriodId(period.getId())
                    .journalType(JournalHeader.JournalType.SYSTEM)
                    .description("Auto-Journal: AR Receipt Posted " + event.getReceiptNumber())
                    .status(JournalHeader.JournalStatus.DRAFT)
                    .sourceSystem("TRANSACTION_SERVICE")
                    .referenceId(event.getId())
                    .referenceNumber(event.getReceiptNumber())
                    .referenceType("AR_RECEIPT")
                    .build();

            JournalHeader savedHeader = journalHeaderRepository.save(header);

            // Cash/Bank (Debit)
            Account bankAccount = resolveOrCreateAccount(tenantIdStr, "1100", "Cash and Bank", Account.AccountType.ASSET);
            JournalLine bankLine = JournalLine.builder()
                    .journalHeader(savedHeader)
                    .accountId(bankAccount.getId())
                    .debitAmount(event.getAmount())
                    .creditAmount(BigDecimal.ZERO)
                    .description("Collection " + event.getReceiptNumber() + (event.getReferenceNumber() != null ? " Ref: " + event.getReferenceNumber() : ""))
                    .build();
            journalLineRepository.save(bankLine);

            // Accounts Receivable (Credit)
            Account arAccount = resolveOrCreateAccount(tenantIdStr, "1200", "Accounts Receivable", Account.AccountType.ASSET);
            JournalLine arLine = JournalLine.builder()
                    .journalHeader(savedHeader)
                    .accountId(arAccount.getId())
                    .debitAmount(BigDecimal.ZERO)
                    .creditAmount(event.getAmount())
                    .description("Collection clearing for " + event.getReceiptNumber())
                    .build();
            journalLineRepository.save(arLine);

            log.info("✅ AR Receipt auto-journal created successfully: {}", savedHeader.getJournalNumber());
        } catch (Exception e) {
            log.error("❌ Failed to process AR Receipt Posted event", e);
        } finally {
            com.financial.corefinance.domain.base.TenantContext.clear();
        }
    }

    private Account resolveOrCreateAccount(String tenantIdStr, String code, String name, Account.AccountType type) {
        return accountRepository.findByTenantIdAndAccountCode(tenantIdStr, code)
            .orElseGet(() -> {
                log.info("Creating default {} account: {} - {}", type, code, name);
                Account account = new Account();
                account.setTenantId(tenantIdStr);
                account.setAccountCode(code);
                account.setAccountName(name);
                account.setAccountType(type);
                account.setIsActive(true);
                return accountRepository.save(account);
            });
    }

    @Data
    public static class SalesInvoiceEventDto {
        private UUID id;
        private UUID tenantId;
        private String invoiceNumber;
        private LocalDate invoiceDate;
        private BigDecimal totalAmount;
    }

    @Data
    public static class ReceiptEventDto {
        private UUID id;
        private UUID tenantId;
        private String receiptNumber;
        private LocalDate receiptDate;
        private BigDecimal amount;
        private String referenceNumber;
    }
}
