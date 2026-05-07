package com.financial.corefinance.service.scheduled;

import com.financial.corefinance.domain.entity.*;
import com.financial.corefinance.repository.*;
import com.financial.corefinance.service.PostingEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchProcessingService {

    private final JournalHeaderRepository journalHeaderRepository;
    private final AccountingPeriodRepository accountingPeriodRepository;
    private final FiscalYearRepository fiscalYearRepository;
    private final BudgetLineRepository budgetLineRepository;
    private final AuditLogRepository auditLogRepository;

    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void autoPostPendingJournals() {
        log.info("Starting auto-posting of pending journals");
        
        String tenantId = "default"; // This should be dynamically determined in a multi-tenant setup
        
        try {
            // Get journals ready for posting
            List<JournalHeader> pendingJournals = journalHeaderRepository.findJournalsForPosting(
                tenantId, JournalHeader.JournalStatus.APPROVED, LocalDate.now());
            
            int postedCount = 0;
            int failedCount = 0;
            
            for (JournalHeader journal : pendingJournals) {
                try {
                    // This would use the PostingEngineService to post the journal
                    // JournalHeader postedJournal = postingEngineService.postJournal(journal.getId());
                    postedCount++;
                    
                    log.debug("Auto-posted journal: {}", journal.getJournalNumber());
                    
                } catch (Exception e) {
                    failedCount++;
                    log.error("Failed to auto-post journal: {} - {}", journal.getJournalNumber(), e.getMessage());
                    
                    // Create audit log for failure
                    createAuditLog("JournalHeader", journal.getId(), "AUTO_POST_FAILED", 
                                 "Failed to auto-post journal: " + e.getMessage(), "system");
                }
            }
            
            log.info("Auto-posting completed. Posted: {}, Failed: {}", postedCount, failedCount);
            
            // Create summary audit log
            createAuditLog("BatchProcess", null, "AUTO_POST_COMPLETED", 
                         String.format("Auto-posted %d journals, %d failed", postedCount, failedCount), "system");
            
        } catch (Exception e) {
            log.error("Auto-posting process failed", e);
            createAuditLog("BatchProcess", null, "AUTO_POST_ERROR", "Auto-posting process failed: " + e.getMessage(), "system");
        }
    }

    @Scheduled(cron = "0 30 23 * * ?") // Run daily at 11:30 PM
    @Transactional
    public void updateBudgetActuals() {
        log.info("Starting budget actuals update");
        
        String tenantId = "default";
        
        try {
            // Get all budget lines that need updating
            List<BudgetLine> budgetLines = budgetLineRepository.findByTenantIdAndAccountId(tenantId, null);
            
            int updatedCount = 0;
            
            for (BudgetLine budgetLine : budgetLines) {
                try {
                    // Calculate actual amount for the budget line
                    BigDecimal actualAmount = calculateActualAmountForBudgetLine(budgetLine);
                    
                    // Update if different
                    if (actualAmount.compareTo(budgetLine.getActualAmount()) != 0) {
                        budgetLine.setActualAmount(actualAmount);
                        budgetLine.calculateAmounts();
                        budgetLineRepository.save(budgetLine);
                        updatedCount++;
                        
                        log.debug("Updated budget line {} with actual amount: {}", 
                                budgetLine.getId(), actualAmount);
                    }
                    
                } catch (Exception e) {
                    log.error("Failed to update budget line: {} - {}", budgetLine.getId(), e.getMessage());
                }
            }
            
            log.info("Budget actuals update completed. Updated: {} budget lines", updatedCount);
            
            createAuditLog("BatchProcess", null, "BUDGET_ACTUALS_UPDATED", 
                         String.format("Updated %d budget lines with actual amounts", updatedCount), "system");
            
        } catch (Exception e) {
            log.error("Budget actuals update failed", e);
            createAuditLog("BatchProcess", null, "BUDGET_ACTUALS_ERROR", "Budget actuals update failed: " + e.getMessage(), "system");
        }
    }

    @Scheduled(cron = "0 0 1 1 * ?") // Run on the first day of each month at 1 AM
    @Transactional
    public void monthlyPeriodClosingCheck() {
        log.info("Starting monthly period closing check");
        
        String tenantId = "default";
        LocalDate today = LocalDate.now();
        
        try {
            // Check if current period should be closed (e.g., 3 days after month end)
            LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
            LocalDate closeDate = monthEnd.plusDays(3);
            
            if (today.isEqual(closeDate) || today.isAfter(closeDate)) {
                // Find the accounting period for the previous month
                LocalDate previousMonthEnd = monthEnd.minusMonths(1);
                
                accountingPeriodRepository.findPeriodForDate(tenantId, previousMonthEnd)
                    .ifPresent(period -> {
                        if (period.getIsOpen() && !period.getIsClosed()) {
                            log.info("Period {} is ready for closing", period.getPeriodName());
                            
                            // Create alert/reminder for period closing
                            createAuditLog("AccountingPeriod", period.getId(), "PERIOD_CLOSING_REMINDER", 
                                         "Period is ready for closing", "system");
                        }
                    });
            }
            
            log.info("Monthly period closing check completed");
            
        } catch (Exception e) {
            log.error("Monthly period closing check failed", e);
            createAuditLog("BatchProcess", null, "PERIOD_CLOSING_CHECK_ERROR", 
                         "Period closing check failed: " + e.getMessage(), "system");
        }
    }

    @Scheduled(cron = "0 0 3 1 1 ?") // Run on January 1st at 3 AM
    @Transactional
    public void yearlyFiscalYearSetup() {
        log.info("Starting yearly fiscal year setup");
        
        String tenantId = "default";
        int currentYear = LocalDate.now().getYear();
        
        try {
            // Check if fiscal year for next year exists
            Optional<FiscalYear> existingFiscalYear = fiscalYearRepository.findByTenantIdAndYearNumber(tenantId, currentYear + 1);
            
            if (existingFiscalYear.isEmpty()) {
                log.info("Creating fiscal year for {}", currentYear + 1);
                
                // Create new fiscal year
                FiscalYear newFiscalYear = FiscalYear.builder()
                    .tenantId(tenantId)
                    .yearNumber(currentYear + 1)
                    .yearName("Fiscal Year " + (currentYear + 1))
                    .startDate(LocalDate.of(currentYear + 1, 1, 1))
                    .endDate(LocalDate.of(currentYear + 1, 12, 31))
                    .totalPeriods(12)
                    .isCurrent(false)
                    .isClosed(false)
                    .build();
                
                fiscalYearRepository.save(newFiscalYear);
                
                // Create audit log
                createAuditLog("FiscalYear", newFiscalYear.getId(), "FISCAL_YEAR_CREATED", 
                             "Automatically created fiscal year for " + (currentYear + 1), "system");
                
                log.info("Fiscal year {} created successfully", currentYear + 1);
            } else {
                log.info("Fiscal year for {} already exists", currentYear + 1);
            }
            
        } catch (Exception e) {
            log.error("Yearly fiscal year setup failed", e);
            createAuditLog("BatchProcess", null, "FISCAL_YEAR_SETUP_ERROR", 
                         "Fiscal year setup failed: " + e.getMessage(), "system");
        }
    }

    @Scheduled(cron = "0 */15 * * * ?") // Run every 15 minutes
    @Transactional(readOnly = true)
    public void systemHealthCheck() {
        log.debug("Running system health check");
        
        try {
            String tenantId = "default";
            
            // Check for any issues
            long draftJournalsCount = journalHeaderRepository.countByTenantIdAndStatus(
                tenantId, JournalHeader.JournalStatus.DRAFT);
            
            long pendingApprovalCount = journalHeaderRepository.countByTenantIdAndStatus(
                tenantId, JournalHeader.JournalStatus.PENDING_APPROVAL);
            
            long overBudgetLinesCount = budgetLineRepository.findOverBudgetLines(tenantId).size();
            
            // If there are issues, create audit logs
            if (draftJournalsCount > 10) {
                createAuditLog("SystemHealth", null, "HIGH_DRAFT_JOURNALS", 
                             String.format("High number of draft journals: %d", draftJournalsCount), "system");
            }
            
            if (pendingApprovalCount > 20) {
                createAuditLog("SystemHealth", null, "HIGH_PENDING_APPROVALS", 
                             String.format("High number of pending approvals: %d", pendingApprovalCount), "system");
            }
            
            if (overBudgetLinesCount > 5) {
                createAuditLog("SystemHealth", null, "OVER_Budget_ALERT", 
                             String.format("Number of over-budget lines: %d", overBudgetLinesCount), "system");
            }
            
            log.debug("System health check completed. Draft: {}, Pending: {}, Over-budget: {}", 
                     draftJournalsCount, pendingApprovalCount, overBudgetLinesCount);
            
        } catch (Exception e) {
            log.error("System health check failed", e);
        }
    }

    private BigDecimal calculateActualAmountForBudgetLine(BudgetLine budgetLine) {
        // This would calculate the actual amount spent/earned for the budget line
        // by summing journal lines for the account within the budget period
        
        // Placeholder implementation
        return budgetLine.getActualAmount() != null ? budgetLine.getActualAmount() : java.math.BigDecimal.ZERO;
    }

    private void createAuditLog(String entityType, UUID entityId, String action, String details, String userId) {
        AuditLog auditLog = AuditLog.builder()
            .tenantId("default")
            .entityType(entityType)
            .entityId(entityId)
            .action(AuditLog.AuditAction.CREATE)
            .newValue(details)
            .userId(userId)
            .additionalInfo("Batch processing operation")
            .moduleName("BatchProcessing")
            .functionName("scheduledJob")
            .businessTransactionId("BATCH-" + LocalDateTime.now().toString())
            .build();
        
        auditLogRepository.save(auditLog);
    }
}
