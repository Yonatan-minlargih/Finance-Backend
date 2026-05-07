package com.financial.corefinance.dto.eventDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceEventDto {

    private String eventType;
    private String entityType;
    private UUID entityId;
    private String tenantId;
    private String userId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTimestamp;
    
    private Map<String, Object> eventData;
    private String correlationId;
    private String sourceService;
    private String targetService;
    private String businessTransactionId;
    
    // Constructor for simple events
    public FinanceEventDto(String eventType, String entityType, UUID entityId, String tenantId, String userId) {
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.eventTimestamp = LocalDateTime.now();
        this.sourceService = "core-finance-service";
    }
    
    // Constructor for events with additional data
    public FinanceEventDto(String eventType, String entityType, UUID entityId, String tenantId, 
                        String userId, Map<String, Object> eventData) {
        this(eventType, entityType, entityId, tenantId, userId);
        this.eventData = eventData;
    }
    
    // Constructor for events with correlation
    public FinanceEventDto(String eventType, String entityType, UUID entityId, String tenantId, 
                        String userId, String correlationId) {
        this(eventType, entityType, entityId, tenantId, userId);
        this.correlationId = correlationId;
    }
    
    // Static factory methods for common event types
    public static FinanceEventDto journalCreated(UUID journalId, String tenantId, String userId) {
        return FinanceEventDto.builder()
            .eventType("JOURNAL_CREATED")
            .entityType("JournalHeader")
            .entityId(journalId)
            .tenantId(tenantId)
            .userId(userId)
            .eventTimestamp(LocalDateTime.now())
            .sourceService("core-finance-service")
            .build();
    }
    
    public static FinanceEventDto journalUpdated(UUID journalId, String tenantId, String userId) {
        return FinanceEventDto.builder()
            .eventType("JOURNAL_UPDATED")
            .entityType("JournalHeader")
            .entityId(journalId)
            .tenantId(tenantId)
            .userId(userId)
            .eventTimestamp(LocalDateTime.now())
            .sourceService("core-finance-service")
            .build();
    }
    
    public static FinanceEventDto journalPosted(UUID journalId, String tenantId, String userId) {
        return FinanceEventDto.builder()
            .eventType("JOURNAL_POSTED")
            .entityType("JournalHeader")
            .entityId(journalId)
            .tenantId(tenantId)
            .userId(userId)
            .eventTimestamp(LocalDateTime.now())
            .sourceService("core-finance-service")
            .build();
    }
    
    public static FinanceEventDto journalReversed(UUID journalId, String tenantId, String userId) {
        return FinanceEventDto.builder()
            .eventType("JOURNAL_REVERSED")
            .entityType("JournalHeader")
            .entityId(journalId)
            .tenantId(tenantId)
            .userId(userId)
            .eventTimestamp(LocalDateTime.now())
            .sourceService("core-finance-service")
            .build();
    }
    
    public static FinanceEventDto accountCreated(UUID accountId, String tenantId, String userId) {
        return FinanceEventDto.builder()
            .eventType("ACCOUNT_CREATED")
            .entityType("Account")
            .entityId(accountId)
            .tenantId(tenantId)
            .userId(userId)
            .eventTimestamp(LocalDateTime.now())
            .sourceService("core-finance-service")
            .build();
    }
    
    public static FinanceEventDto accountUpdated(UUID accountId, String tenantId, String userId) {
        return FinanceEventDto.builder()
            .eventType("ACCOUNT_UPDATED")
            .entityType("Account")
            .entityId(accountId)
            .tenantId(tenantId)
            .userId(userId)
            .eventTimestamp(LocalDateTime.now())
            .sourceService("core-finance-service")
            .build();
    }
    
    public static FinanceEventDto budgetCreated(UUID budgetId, String tenantId, String userId) {
        return FinanceEventDto.builder()
            .eventType("BUDGET_CREATED")
            .entityType("Budget")
            .entityId(budgetId)
            .tenantId(tenantId)
            .userId(userId)
            .eventTimestamp(LocalDateTime.now())
            .sourceService("core-finance-service")
            .build();
    }
    
    public static FinanceEventDto budgetUpdated(UUID budgetId, String tenantId, String userId) {
        return FinanceEventDto.builder()
            .eventType("BUDGET_UPDATED")
            .entityType("Budget")
            .entityId(budgetId)
            .tenantId(tenantId)
            .userId(userId)
            .eventTimestamp(LocalDateTime.now())
            .sourceService("core-finance-service")
            .build();
    }
    
    public static FinanceEventDto fiscalYearCreated(UUID fiscalYearId, String tenantId, String userId) {
        return FinanceEventDto.builder()
            .eventType("FISCAL_YEAR_CREATED")
            .entityType("FiscalYear")
            .entityId(fiscalYearId)
            .tenantId(tenantId)
            .userId(userId)
            .eventTimestamp(LocalDateTime.now())
            .sourceService("core-finance-service")
            .build();
    }
    
    public static FinanceEventDto fiscalYearClosed(UUID fiscalYearId, String tenantId, String userId) {
        return FinanceEventDto.builder()
            .eventType("FISCAL_YEAR_CLOSED")
            .entityType("FiscalYear")
            .entityId(fiscalYearId)
            .tenantId(tenantId)
            .userId(userId)
            .eventTimestamp(LocalDateTime.now())
            .sourceService("core-finance-service")
            .build();
    }
    
    public static FinanceEventDto reportGenerated(UUID reportId, String tenantId, String userId) {
        return FinanceEventDto.builder()
            .eventType("REPORT_GENERATED")
            .entityType("FinancialReport")
            .entityId(reportId)
            .tenantId(tenantId)
            .userId(userId)
            .eventTimestamp(LocalDateTime.now())
            .sourceService("core-finance-service")
            .build();
    }
}
