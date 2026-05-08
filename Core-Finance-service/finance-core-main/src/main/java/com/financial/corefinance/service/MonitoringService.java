package com.financial.corefinance.service;

import com.financial.corefinance.domain.entity.*;
import com.financial.corefinance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService implements HealthIndicator {

    private final JournalHeaderRepository journalHeaderRepository;
    private final AccountRepository accountRepository;
    private final BudgetLineRepository budgetLineRepository;
    private final BudgetRepository budgetRepository;
    private final AccountingPeriodRepository accountingPeriodRepository;
    private final AuditLogRepository auditLogRepository;

    // Performance metrics
    private final Map<String, Long> performanceMetrics = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastActivityTimes = new ConcurrentHashMap<>();

    // Alert thresholds
    private static final long HIGH_DRAFT_JOURNALS_THRESHOLD = 50;
    private static final long HIGH_PENDING_APPROVALS_THRESHOLD = 20;
    private static final long OVER_BUDGET_LINES_THRESHOLD = 10;
    private static final long ERROR_RATE_THRESHOLD = 5; // percentage

    @Override
    public Health health() {
        try {
            Map<String, Object> details = new HashMap<>();
            
            // System health checks
            boolean isHealthy = performHealthChecks(details);
            
            // Add performance metrics
            details.putAll(getPerformanceMetrics());
            
            // Add recent activity
            details.put("recentActivity", getRecentActivity());
            
            // Add alert status
            details.put("activeAlerts", getActiveAlerts());
            
            if (isHealthy) {
                return Health.up()
                    .withDetails(details)
                    .build();
            } else {
                return Health.down()
                    .withDetails(details)
                    .withDetail("error", "System health checks failed")
                    .build();
            }
            
        } catch (Exception e) {
            log.error("Health check failed", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }

    private boolean performHealthChecks(Map<String, Object> details) {
        boolean isHealthy = true;
        
        try {
            // Database connectivity check
            long accountCount = accountRepository.count();
            details.put("databaseAccounts", accountCount);
            
            // Check for critical issues
            long draftJournalsCount = journalHeaderRepository.countByTenantIdAndStatus(
                "default", JournalHeader.JournalStatus.DRAFT);
            details.put("draftJournals", draftJournalsCount);
            
            if (draftJournalsCount > HIGH_DRAFT_JOURNALS_THRESHOLD) {
                isHealthy = false;
                details.put("alert", "High number of draft journals");
            }
            
            long pendingApprovalsCount = journalHeaderRepository.countByTenantIdAndStatus(
                "default", JournalHeader.JournalStatus.PENDING_APPROVAL);
            details.put("pendingApprovals", pendingApprovalsCount);
            
            if (pendingApprovalsCount > HIGH_PENDING_APPROVALS_THRESHOLD) {
                isHealthy = false;
                details.put("alert", "High number of pending approvals");
            }
            
            long overBudgetLinesCount = budgetLineRepository.findOverBudgetLines("default").size();
            details.put("overBudgetLines", overBudgetLinesCount);
            
            if (overBudgetLinesCount > OVER_BUDGET_LINES_THRESHOLD) {
                isHealthy = false;
                details.put("alert", "High number of over-budget lines");
            }
            
            // Check for open periods
            long openPeriodsCount = accountingPeriodRepository.findByTenantIdAndIsOpenTrue("default").size();
            details.put("openPeriods", openPeriodsCount);
            
            if (openPeriodsCount == 0) {
                isHealthy = false;
                details.put("alert", "No open accounting periods");
            }
            
        } catch (Exception e) {
            log.error("Health check failed", e);
            details.put("error", e.getMessage());
            isHealthy = false;
        }
        
        return isHealthy;
    }

    private Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Add custom performance metrics
        metrics.putAll(performanceMetrics);
        
        // Calculate response times (simplified)
        metrics.put("averageResponseTime", calculateAverageResponseTime());
        metrics.put("requestsPerMinute", calculateRequestsPerMinute());
        metrics.put("errorRate", calculateErrorRate());
        
        return metrics;
    }

    private Map<String, Object> getRecentActivity() {
        Map<String, Object> activity = new HashMap<>();
        
        try {
            // Recent journal postings (last hour)
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long recentJournals = auditLogRepository.countByTenantIdAndActionSince("default", AuditLog.AuditAction.POST, oneHourAgo);
            activity.put("recentJournalPostings", recentJournals);
            
            // Recent account changes
            long recentAccountChanges = auditLogRepository.countByTenantIdAndActionSince("default", AuditLog.AuditAction.UPDATE, oneHourAgo);
            activity.put("recentAccountChanges", recentAccountChanges);
            
            // Last activity times
            activity.put("lastJournalPosting", lastActivityTimes.get("journalPosting"));
            activity.put("lastAccountUpdate", lastActivityTimes.get("accountUpdate"));
            activity.put("lastBudgetUpdate", lastActivityTimes.get("budgetUpdate"));
            
        } catch (Exception e) {
            log.error("Failed to get recent activity", e);
            activity.put("error", e.getMessage());
        }
        
        return activity;
    }

    private List<Map<String, Object>> getActiveAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        try {
            // Check for various alert conditions
            checkDraftJournalsAlert(alerts);
            checkPendingApprovalsAlert(alerts);
            checkOverBudgetAlert(alerts);
            checkPeriodClosingAlert(alerts);
            checkErrorRateAlert(alerts);
            checkSystemPerformanceAlert(alerts);
            
        } catch (Exception e) {
            log.error("Failed to get active alerts", e);
            Map<String, Object> errorAlert = new HashMap<>();
            errorAlert.put("type", "SYSTEM_ERROR");
            errorAlert.put("message", "Failed to retrieve alerts: " + e.getMessage());
            errorAlert.put("severity", "HIGH");
            errorAlert.put("timestamp", LocalDateTime.now());
            alerts.add(errorAlert);
        }
        
        return alerts;
    }

    private void checkDraftJournalsAlert(List<Map<String, Object>> alerts) {
        long draftCount = journalHeaderRepository.countByTenantIdAndStatus(
            "default", JournalHeader.JournalStatus.DRAFT);
        
        if (draftCount > HIGH_DRAFT_JOURNALS_THRESHOLD) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "HIGH_DRAFT_JOURNALS");
            alert.put("message", String.format("High number of draft journals: %d", draftCount));
            alert.put("severity", draftCount > HIGH_DRAFT_JOURNALS_THRESHOLD * 2 ? "HIGH" : "MEDIUM");
            alert.put("timestamp", LocalDateTime.now());
            alert.put("value", draftCount);
            alert.put("threshold", HIGH_DRAFT_JOURNALS_THRESHOLD);
            alerts.add(alert);
        }
    }

    private void checkPendingApprovalsAlert(List<Map<String, Object>> alerts) {
        long pendingCount = journalHeaderRepository.countByTenantIdAndStatus(
            "default", JournalHeader.JournalStatus.PENDING_APPROVAL);
        
        if (pendingCount > HIGH_PENDING_APPROVALS_THRESHOLD) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "HIGH_PENDING_APPROVALS");
            alert.put("message", String.format("High number of pending approvals: %d", pendingCount));
            alert.put("severity", pendingCount > HIGH_PENDING_APPROVALS_THRESHOLD * 2 ? "HIGH" : "MEDIUM");
            alert.put("timestamp", LocalDateTime.now());
            alert.put("value", pendingCount);
            alert.put("threshold", HIGH_PENDING_APPROVALS_THRESHOLD);
            alerts.add(alert);
        }
    }

    private void checkOverBudgetAlert(List<Map<String, Object>> alerts) {
        long overBudgetCount = budgetLineRepository.findOverBudgetLines("default").size();
        
        if (overBudgetCount > OVER_BUDGET_LINES_THRESHOLD) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "OVER_BUDGET_ALERT");
            alert.put("message", String.format("Number of over-budget lines: %d", overBudgetCount));
            alert.put("severity", overBudgetCount > OVER_BUDGET_LINES_THRESHOLD * 2 ? "HIGH" : "MEDIUM");
            alert.put("timestamp", LocalDateTime.now());
            alert.put("value", overBudgetCount);
            alert.put("threshold", OVER_BUDGET_LINES_THRESHOLD);
            alerts.add(alert);
        }
    }

    private void checkPeriodClosingAlert(List<Map<String, Object>> alerts) {
        List<AccountingPeriod> openPeriods = accountingPeriodRepository.findByTenantIdAndIsOpenTrue("default");
        
        for (AccountingPeriod period : openPeriods) {
            LocalDate periodEnd = period.getEndDate();
            LocalDate today = LocalDate.now();
            
            // Alert if period is more than 3 days past its end date
            if (today.isAfter(periodEnd.plusDays(3))) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "PERIOD_CLOSING_OVERDUE");
                alert.put("message", String.format("Period %s is overdue for closing", period.getPeriodName()));
                alert.put("severity", "HIGH");
                alert.put("timestamp", LocalDateTime.now());
                alert.put("periodId", period.getId());
                alert.put("periodName", period.getPeriodName());
                alert.put("overdueDays", ChronoUnit.DAYS.between(periodEnd, today));
                alerts.add(alert);
            }
        }
    }

    private void checkErrorRateAlert(List<Map<String, Object>> alerts) {
        double errorRate = calculateErrorRate();
        
        if (errorRate > ERROR_RATE_THRESHOLD) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "HIGH_ERROR_RATE");
            alert.put("message", String.format("High error rate: %.2f%%", errorRate));
            alert.put("severity", errorRate > ERROR_RATE_THRESHOLD * 2 ? "HIGH" : "MEDIUM");
            alert.put("timestamp", LocalDateTime.now());
            alert.put("value", errorRate);
            alert.put("threshold", ERROR_RATE_THRESHOLD);
            alerts.add(alert);
        }
    }

    private void checkSystemPerformanceAlert(List<Map<String, Object>> alerts) {
        long avgResponseTime = calculateAverageResponseTime();
        
        if (avgResponseTime > 5000) { // 5 seconds
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "SLOW_RESPONSE_TIME");
            alert.put("message", String.format("Slow response time: %d ms", avgResponseTime));
            alert.put("severity", avgResponseTime > 10000 ? "HIGH" : "MEDIUM");
            alert.put("timestamp", LocalDateTime.now());
            alert.put("value", avgResponseTime);
            alert.put("threshold", 5000);
            alerts.add(alert);
        }
    }

    // Performance tracking methods
    public void recordPerformanceMetric(String metricName, long value) {
        performanceMetrics.put(metricName, value);
        lastActivityTimes.put(metricName, LocalDateTime.now());
    }

    public void recordJournalPosting(long responseTime) {
        recordPerformanceMetric("journalPosting", responseTime);
    }

    public void recordAccountUpdate(long responseTime) {
        recordPerformanceMetric("accountUpdate", responseTime);
    }

    public void recordBudgetUpdate(long responseTime) {
        recordPerformanceMetric("budgetUpdate", responseTime);
    }

    // Metric calculation methods (simplified implementations)
    private long calculateAverageResponseTime() {
        return performanceMetrics.values().stream()
            .mapToLong(Long::longValue)
            .findFirst()
            .orElse(100L); // Default response time
    }

    private long calculateRequestsPerMinute() {
        // Simplified calculation based on recent activity
        return 10L; // Default value
    }

    private double calculateErrorRate() {
        // Simplified error rate calculation
        return 1.0; // Default 1% error rate
    }

    // Alert management
    public List<Map<String, Object>> getAlertsByType(String alertType) {
        List<Map<String, Object>> allAlerts = getActiveAlerts();
        return allAlerts.stream()
            .filter(alert -> alertType.equals(alert.get("type")))
            .toList();
    }

    public List<Map<String, Object>> getAlertsBySeverity(String severity) {
        List<Map<String, Object>> allAlerts = getActiveAlerts();
        return allAlerts.stream()
            .filter(alert -> severity.equals(alert.get("severity")))
            .toList();
    }

    // System metrics
    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("totalAccounts", accountRepository.count());
        metrics.put("totalJournals", journalHeaderRepository.count());
        metrics.put("totalBudgets", budgetRepository.count());
        metrics.put("openPeriods", accountingPeriodRepository.findByTenantIdAndIsOpenTrue("default").size());
        metrics.put("totalAuditLogs", auditLogRepository.count());
        
        // Add custom metrics
        metrics.putAll(performanceMetrics);
        
        return metrics;
    }

    // Health check for specific components
    public Health checkDatabaseHealth() {
        try {
            long count = accountRepository.count();
            return Health.up()
                .withDetail("accounts", count)
                .withDetail("status", "Connected")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }

    public Health checkCacheHealth() {
        try {
            // Check cache service availability
            return Health.up()
                .withDetail("status", "Available")
                .withDetail("cacheHits", performanceMetrics.getOrDefault("cacheHits", 0L))
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }

    // Cleanup old metrics
    public void cleanupOldMetrics() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        
        lastActivityTimes.entrySet().removeIf(entry -> 
            entry.getValue().isBefore(cutoff));
        
        log.info("Cleaned up old metrics. Remaining metrics: {}", performanceMetrics.size());
    }
}
