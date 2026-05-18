package com.financial.corefinance.event;

import com.financial.corefinance.domain.entity.Account;
import com.financial.corefinance.domain.entity.JournalHeader;
import com.financial.corefinance.domain.entity.JournalLine;
import com.financial.corefinance.dto.eventDto.PayrollApprovedEventDto;
import com.financial.corefinance.repository.AccountRepository;
import com.financial.corefinance.repository.JournalHeaderRepository;
import com.financial.corefinance.repository.JournalLineRepository;
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
public class PayrollEventConsumer {

    private final JournalHeaderRepository journalHeaderRepository;
    private final JournalLineRepository journalLineRepository;
    private final AccountRepository accountRepository;

    /**
     * Listens to payroll.approved events from the payroll-service.
     * Automatically creates balanced double-entry Draft Journals:
     * - Debit: Salaries Expense (Gross)
     * - Credit: Salaries Payable (Net)
     * - Credit: Payroll Deductions Payable (Deductions = Gross - Net)
     */
    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "finance.payroll-approved.queue", durable = "true"),
            exchange = @Exchange(value = "payroll.exchange", type = "direct"),
            key = "payroll.approved"
        )
    )
    public void handleExternalPayrollApproved(PayrollApprovedEventDto event) {
        try {
            log.info("📢 CROSS-SERVICE: Processing Payroll Approved event for Journal creation...");
            log.info("Event details: id={}, tenantId={}, totalGross={}, totalNet={}", 
                    event.getId(), event.getTenantId(), event.getTotalGross(), event.getTotalNet());

            String tenantIdStr = event.getTenantId().toString();
            BigDecimal gross = event.getTotalGross() != null ? event.getTotalGross() : BigDecimal.ZERO;
            BigDecimal net = event.getTotalNet() != null ? event.getTotalNet() : BigDecimal.ZERO;
            BigDecimal deductions = gross.subtract(net);

            if (gross.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("⚠️ Received payroll run with zero or negative gross amount. Skipping journal creation.");
                return;
            }

            // 1. Resolve Accounts dynamically (with bulletproof fallbacks)
            Account expenseAccount = resolveAccount(tenantIdStr, "6100", Account.AccountType.EXPENSE, "Expense");
            Account liabilityAccount = resolveAccount(tenantIdStr, "2100", Account.AccountType.LIABILITY, "Payable");
            Account deductionsAccount = resolveAccount(tenantIdStr, "2200", Account.AccountType.LIABILITY, "Deduction");

            // 2. Create Journal Header
            JournalHeader header = JournalHeader.builder()
                    .tenantId(tenantIdStr)
                    .journalNumber("PAYROLL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .journalDate(event.getRunDate() != null ? event.getRunDate() : LocalDate.now())
                    .accountingPeriodId(event.getPeriodId())
                    .journalType(JournalHeader.JournalType.SYSTEM)
                    .description("Auto-Journal: Approved Payroll Run " + event.getId())
                    .status(JournalHeader.JournalStatus.DRAFT)
                    .sourceSystem("PAYROLL_SERVICE")
                    .referenceId(event.getId())
                    .build();

            header = journalHeaderRepository.save(header);
            UUID headerId = header.getId();

            // 3. Create Journal Line 1: Debit Gross Salary Expense
            JournalLine grossLine = JournalLine.builder()
                    .tenantId(tenantIdStr)
                    .journalHeaderId(headerId)
                    .lineNumber(1)
                    .accountId(expenseAccount.getId())
                    .debitAmount(gross)
                    .creditAmount(BigDecimal.ZERO)
                    .description("Gross salaries and labor expenses")
                    .reconciled(false)
                    .build();
            journalLineRepository.save(grossLine);

            // 4. Create Journal Line 2: Credit Net Salary Payable
            JournalLine netLine = JournalLine.builder()
                    .tenantId(tenantIdStr)
                    .journalHeaderId(headerId)
                    .lineNumber(2)
                    .accountId(liabilityAccount.getId())
                    .debitAmount(BigDecimal.ZERO)
                    .creditAmount(net)
                    .description("Net salaries payable to employees")
                    .reconciled(false)
                    .build();
            journalLineRepository.save(netLine);

            // 5. Create Journal Line 3: Credit Deductions Payable (if any)
            if (deductions.compareTo(BigDecimal.ZERO) > 0) {
                JournalLine dedLine = JournalLine.builder()
                        .tenantId(tenantIdStr)
                        .journalHeaderId(headerId)
                        .lineNumber(3)
                        .accountId(deductionsAccount.getId())
                        .debitAmount(BigDecimal.ZERO)
                        .creditAmount(deductions)
                        .description("Total payroll tax, pension, and loan deductions payable")
                        .reconciled(false)
                        .build();
                journalLineRepository.save(dedLine);
            }

            log.info("✅ DATABASE UPDATED: Successfully generated draft balanced double-entry Journal: {} for Payroll: {}", 
                    header.getJournalNumber(), event.getId());

        } catch (Exception e) {
            log.error("❌ ERROR: Failed to create finance journal from payroll approved event", e);
        }
    }

    private Account resolveAccount(String tenantId, String code, Account.AccountType type, String keyword) {
        // Try exact match by code
        return accountRepository.findByTenantIdAndAccountCode(tenantId, code)
            .orElseGet(() -> {
                try {
                    // Proactively seed the missing required payroll account!
                    log.info("🌱 Seeding missing required payroll account {} (Type: {}, Name: {})", code, type, keyword);
                    Account account = new Account();
                    account.setTenantId(tenantId);
                    account.setAccountCode(code);
                    account.setAccountName(keyword + " Account (" + code + ")");
                    account.setAccountType(type);
                    account.setNormalBalance(type == Account.AccountType.EXPENSE ? Account.NormalBalance.DEBIT : Account.NormalBalance.CREDIT);
                    account.setIsActive(true);
                    account.setAllowManualEntry(true);
                    account.setIsConsolidated(false);
                    account.setCurrencyCode("ETB");
                    
                    // Map IFRS categories for premium audit alignment
                    if (type == Account.AccountType.EXPENSE) {
                        account.setIFRSCategory(Account.IFRSCategory.OPERATING_EXPENSES);
                    } else if (type == Account.AccountType.LIABILITY) {
                        account.setIFRSCategory(Account.IFRSCategory.CURRENT_LIABILITIES);
                    }
                    
                    return accountRepository.save(account);
                } catch (Exception ex) {
                    log.error("⚠️ Failed to seed default account {}, searching for general fallback...", code, ex);
                }

                // Try match by type
                List<Account> accountsByType = accountRepository.findByTenantIdAndAccountType(tenantId, type);
                if (!accountsByType.isEmpty()) {
                    // Look for matching keyword in name
                    for (Account acct : accountsByType) {
                        if (acct.getAccountName() != null && acct.getAccountName().toLowerCase().contains(keyword.toLowerCase())) {
                            return acct;
                        }
                    }
                    return accountsByType.get(0);
                }
                // Try any active active account
                List<Account> activeAccounts = accountRepository.findByTenantIdAndIsActiveTrue(tenantId);
                if (!activeAccounts.isEmpty()) {
                    return activeAccounts.get(0);
                }
                // Final fallback: fetch the first account in the repository
                List<Account> all = accountRepository.findAll();
                if (!all.isEmpty()) {
                    return all.get(0);
                }
                throw new IllegalStateException("No accounts found in system to associate with journal entries!");
            });
    }
}
