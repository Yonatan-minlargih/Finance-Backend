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
public class ApEventConsumer {

    private final JournalHeaderRepository journalHeaderRepository;
    private final JournalLineRepository journalLineRepository;
    private final AccountRepository accountRepository;
    private final AccountingPeriodRepository accountingPeriodRepository;

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.invoiceApprovedQueue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.transactionalEventsExchange}", type = "direct"),
            key = "${rabbitmq.invoiceApprovedQueue}"
        )
    )
    public void handleInvoiceApproved(InvoiceEventDto event) {
        try {
            log.info("📢 CROSS-SERVICE: Processing Invoice Approved event for Journal creation...");
            log.info("Event details: id={}, tenantId={}, invoiceNumber={}, totalAmount={}", 
                    event.getId(), event.getTenantId(), event.getInvoiceNumber(), event.getTotalAmount());

            String tenantIdStr = event.getTenantId().toString();
            BigDecimal amount = event.getTotalAmount() != null ? event.getTotalAmount() : BigDecimal.ZERO;

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("⚠️ Received invoice with zero or negative amount. Skipping journal creation.");
                return;
            }

            // Resolve accounts
            Account expenseAccount = resolveAccount(tenantIdStr, "5100", Account.AccountType.EXPENSE, "Expense");
            Account apAccount = resolveAccount(tenantIdStr, "2100", Account.AccountType.LIABILITY, "Payable");

            // Find accounting period
            LocalDate invoiceDate = event.getInvoiceDate() != null ? event.getInvoiceDate() : LocalDate.now();
            UUID periodId = accountingPeriodRepository.findPeriodForDate(tenantIdStr, invoiceDate)
                    .map(AccountingPeriod::getId)
                    .orElseGet(() -> {
                        List<AccountingPeriod> open = accountingPeriodRepository.findByTenantIdAndIsOpenTrue(tenantIdStr);
                        return open.isEmpty() ? null : open.get(0).getId();
                    });

            if (periodId == null) {
                log.error("❌ No open accounting period found for date {}. Skipping invoice journal.", invoiceDate);
                return;
            }

            // Create Journal Header
            String typeName = (event.getInvoiceType() != null) 
                    ? event.getInvoiceType().replace("_", " ") 
                    : "Invoice";
            // Capitalize first letter of each word
            if (typeName.length() > 0) {
                String[] words = typeName.toLowerCase().split(" ");
                StringBuilder sb = new StringBuilder();
                for (String w : words) {
                    if (w.length() > 0) {
                        sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
                    }
                }
                typeName = sb.toString().trim();
            }

            JournalHeader header = JournalHeader.builder()
                    .tenantId(tenantIdStr)
                    .journalNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .journalDate(invoiceDate)
                    .accountingPeriodId(periodId)
                    .journalType(JournalHeader.JournalType.SYSTEM)
                    .description("Auto-Journal: AP " + typeName + " Approved " + event.getInvoiceNumber())
                    .status(JournalHeader.JournalStatus.DRAFT)
                    .sourceSystem("TRANSACTION_SERVICE")
                    .referenceId(event.getId())
                    .referenceNumber(event.getInvoiceNumber())
                    .referenceType("AP_INVOICE")
                    .build();

            header = journalHeaderRepository.save(header);
            UUID headerId = header.getId();

            int lineNum = 1;
            BigDecimal aggregatedLineAmount = BigDecimal.ZERO;
            if (event.getLines() != null && !event.getLines().isEmpty()) {
                for (InvoiceLineEventDto line : event.getLines()) {
                    Account lineExpenseAccount = expenseAccount;
                    if (line.getAccountId() != null && !line.getAccountId().trim().isEmpty()) {
                        try {
                            UUID accountUuid = UUID.fromString(line.getAccountId());
                            lineExpenseAccount = accountRepository.findById(accountUuid)
                                    .orElseGet(() -> resolveAccount(tenantIdStr, line.getAccountId(), Account.AccountType.EXPENSE, "Expense"));
                        } catch (IllegalArgumentException e) {
                            lineExpenseAccount = resolveAccount(tenantIdStr, line.getAccountId(), Account.AccountType.EXPENSE, "Expense");
                        }
                    }
                    BigDecimal lineAmt = line.getLineAmount() != null ? line.getLineAmount() : BigDecimal.ZERO;
                    aggregatedLineAmount = aggregatedLineAmount.add(lineAmt);

                    JournalLine debitLine = JournalLine.builder()
                            .tenantId(tenantIdStr)
                            .journalHeaderId(headerId)
                            .lineNumber(lineNum++)
                            .accountId(lineExpenseAccount.getId())
                            .debitAmount(lineAmt)
                            .creditAmount(BigDecimal.ZERO)
                            .description(line.getDescription() != null ? line.getDescription() : ("AP Invoice Expense line: " + event.getInvoiceNumber()))
                            .reconciled(false)
                            .build();
                    journalLineRepository.save(debitLine);
                }
            } else {
                aggregatedLineAmount = amount;
                JournalLine debitLine = JournalLine.builder()
                        .tenantId(tenantIdStr)
                        .journalHeaderId(headerId)
                        .lineNumber(lineNum++)
                        .accountId(expenseAccount.getId())
                        .debitAmount(amount)
                        .creditAmount(BigDecimal.ZERO)
                        .description("AP Invoice Expense: " + event.getInvoiceNumber())
                        .reconciled(false)
                        .build();
                journalLineRepository.save(debitLine);
            }

            // Credit Accounts Payable Account
            JournalLine creditLine = JournalLine.builder()
                    .tenantId(tenantIdStr)
                    .journalHeaderId(headerId)
                    .lineNumber(lineNum)
                    .accountId(apAccount.getId())
                    .debitAmount(BigDecimal.ZERO)
                    .creditAmount(aggregatedLineAmount)
                    .description("AP Invoice Payable liability: " + event.getInvoiceNumber())
                    .reconciled(false)
                    .build();
            journalLineRepository.save(creditLine);

            log.info("✅ DATABASE UPDATED: Successfully generated draft journal: {} for Invoice: {}", 
                    header.getJournalNumber(), event.getInvoiceNumber());

        } catch (Exception e) {
            log.error("❌ ERROR: Failed to create finance journal from invoice approved event", e);
        }
    }

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.paymentPostedQueue}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.transactionalEventsExchange}", type = "direct"),
            key = "${rabbitmq.paymentPostedQueue}"
        )
    )
    public void handlePaymentPosted(PaymentEventDto event) {
        try {
            log.info("📢 CROSS-SERVICE: Processing Payment Posted event for Journal creation...");
            log.info("Event details: id={}, tenantId={}, paymentNumber={}, amount={}", 
                    event.getId(), event.getTenantId(), event.getPaymentNumber(), event.getAmount());

            String tenantIdStr = event.getTenantId().toString();
            BigDecimal amount = event.getAmount() != null ? event.getAmount() : BigDecimal.ZERO;

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("⚠️ Received payment with zero or negative amount. Skipping journal creation.");
                return;
            }

            // Resolve accounts
            Account apAccount = resolveAccount(tenantIdStr, "2100", Account.AccountType.LIABILITY, "Payable");
            Account bankAccount = resolveAccount(tenantIdStr, "1100", Account.AccountType.ASSET, "Bank");

            // Find accounting period
            LocalDate paymentDate = event.getPaymentDate() != null ? event.getPaymentDate() : LocalDate.now();
            UUID periodId = accountingPeriodRepository.findPeriodForDate(tenantIdStr, paymentDate)
                    .map(AccountingPeriod::getId)
                    .orElseGet(() -> {
                        List<AccountingPeriod> open = accountingPeriodRepository.findByTenantIdAndIsOpenTrue(tenantIdStr);
                        return open.isEmpty() ? null : open.get(0).getId();
                    });

            if (periodId == null) {
                log.error("❌ No open accounting period found for date {}. Skipping payment journal.", paymentDate);
                return;
            }

            // Create Journal Header
            JournalHeader header = JournalHeader.builder()
                    .tenantId(tenantIdStr)
                    .journalNumber("PMT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .journalDate(paymentDate)
                    .accountingPeriodId(periodId)
                    .journalType(JournalHeader.JournalType.SYSTEM)
                    .description("Auto-Journal: AP Payment Posted " + event.getPaymentNumber())
                    .status(JournalHeader.JournalStatus.DRAFT)
                    .sourceSystem("TRANSACTION_SERVICE")
                    .referenceId(event.getId())
                    .referenceNumber(event.getPaymentNumber())
                    .referenceType("AP_PAYMENT")
                    .build();

            header = journalHeaderRepository.save(header);
            UUID headerId = header.getId();

            // Line 1: Debit AP Account (reduces liability)
            JournalLine debitLine = JournalLine.builder()
                    .tenantId(tenantIdStr)
                    .journalHeaderId(headerId)
                    .lineNumber(1)
                    .accountId(apAccount.getId())
                    .debitAmount(amount)
                    .creditAmount(BigDecimal.ZERO)
                    .description("AP Payment settlement debit: " + event.getPaymentNumber())
                    .reconciled(false)
                    .build();
            journalLineRepository.save(debitLine);

            // Line 2: Credit Cash/Bank Account (reduces asset)
            JournalLine creditLine = JournalLine.builder()
                    .tenantId(tenantIdStr)
                    .journalHeaderId(headerId)
                    .lineNumber(2)
                    .accountId(bankAccount.getId())
                    .debitAmount(BigDecimal.ZERO)
                    .creditAmount(amount)
                    .description("AP Payment bank credit: " + event.getPaymentNumber())
                    .reconciled(false)
                    .build();
            journalLineRepository.save(creditLine);

            log.info("✅ DATABASE UPDATED: Successfully generated draft journal: {} for Payment: {}", 
                    header.getJournalNumber(), event.getPaymentNumber());

        } catch (Exception e) {
            log.error("❌ ERROR: Failed to create finance journal from payment posted event", e);
        }
    }

    private Account resolveAccount(String tenantId, String code, Account.AccountType type, String keyword) {
        return accountRepository.findByTenantIdAndAccountCode(tenantId, code)
            .orElseGet(() -> {
                try {
                    log.info("🌱 Seeding missing required AP account {} (Type: {}, Name: {})", code, type, keyword);
                    Account account = new Account();
                    account.setTenantId(tenantId);
                    account.setAccountCode(code);
                    account.setAccountName(keyword + " Account (" + code + ")");
                    account.setAccountType(type);
                    account.setNormalBalance(type == Account.AccountType.EXPENSE || type == Account.AccountType.ASSET 
                        ? Account.NormalBalance.DEBIT : Account.NormalBalance.CREDIT);
                    account.setIsActive(true);
                    account.setAllowManualEntry(true);
                    account.setIsConsolidated(false);
                    account.setCurrencyCode("ETB");
                    
                    if (type == Account.AccountType.EXPENSE) {
                        account.setIFRSCategory(Account.IFRSCategory.OPERATING_EXPENSES);
                    } else if (type == Account.AccountType.LIABILITY) {
                        account.setIFRSCategory(Account.IFRSCategory.CURRENT_LIABILITIES);
                    } else if (type == Account.AccountType.ASSET) {
                        account.setIFRSCategory(Account.IFRSCategory.CURRENT_ASSETS);
                    }
                    
                    return accountRepository.save(account);
                } catch (Exception ex) {
                    log.error("⚠️ Failed to seed default account {}, searching for general fallback...", code, ex);
                }

                List<Account> accountsByType = accountRepository.findByTenantIdAndAccountType(tenantId, type);
                if (!accountsByType.isEmpty()) {
                    for (Account acct : accountsByType) {
                        if (acct.getAccountName() != null && acct.getAccountName().toLowerCase().contains(keyword.toLowerCase())) {
                            return acct;
                        }
                    }
                    return accountsByType.get(0);
                }
                List<Account> activeAccounts = accountRepository.findByTenantIdAndIsActiveTrue(tenantId);
                if (!activeAccounts.isEmpty()) {
                    return activeAccounts.get(0);
                }
                List<Account> all = accountRepository.findAll();
                if (!all.isEmpty()) {
                    return all.get(0);
                }
                throw new IllegalStateException("No accounts found in system to associate with journal entries!");
            });
    }

    @Data
    public static class InvoiceEventDto {
        private UUID id;
        private UUID tenantId;
        private String invoiceNumber;
        private LocalDate invoiceDate;
        private BigDecimal totalAmount;
        private String invoiceType;
        private List<InvoiceLineEventDto> lines;
    }

    @Data
    public static class InvoiceLineEventDto {
        private UUID id;
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineAmount;
        private String accountId;
    }

    @Data
    public static class PaymentEventDto {
        private UUID id;
        private UUID tenantId;
        private String paymentNumber;
        private LocalDate paymentDate;
        private BigDecimal amount;
    }
}
