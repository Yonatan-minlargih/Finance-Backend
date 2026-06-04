package com.finance.transactional.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApAgingReportLineDto {
    private UUID invoiceId;
    private String invoiceNumber;
    private UUID vendorId;
    private String vendorName;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BigDecimal outstandingAmount;
    private long daysOld;
    private String agingBucket;
    private String currency;
    private String status;
}
