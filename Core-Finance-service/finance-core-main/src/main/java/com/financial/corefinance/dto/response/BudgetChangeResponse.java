package com.financial.corefinance.dto.response;

import com.financial.corefinance.domain.entity.BudgetChange.ChangeType;
import com.financial.corefinance.domain.entity.BudgetChange.ChangeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetChangeResponse {
    private UUID id;
    private String tenantId;
    private UUID budgetVersionId;
    private String versionName;
    private UUID budgetLineId;
    private String accountCode;
    private String accountName;
    private ChangeType changeType;
    private BigDecimal oldAmount;
    private BigDecimal newAmount;
    private BigDecimal changeAmount;
    private BigDecimal changePercentage;
    private String reason;
    private String authorityLevel;
    private String approvedBy;
    private java.time.LocalDateTime approvedAt;
    private ChangeStatus status;
    private LocalDate effectiveDate;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
}
