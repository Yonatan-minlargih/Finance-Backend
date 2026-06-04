package com.finance.transactional.dto.corefinance;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

/** Mirror of Core-Finance {@code AccountingPeriodResponse} for Feign. */
@Data
public class AccountingPeriodLookupDto {

    private UUID id;
    private String tenantId;
    private UUID fiscalYearId;
    private String fiscalYearName;
    private Integer periodNumber;
    private String periodName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isOpen;
    private Boolean isClosed;
    private Boolean isAdjustmentPeriod;
    private String description;
}
