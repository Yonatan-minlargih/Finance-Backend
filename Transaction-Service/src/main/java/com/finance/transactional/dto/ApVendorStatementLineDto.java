package com.finance.transactional.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApVendorStatementLineDto {
    private LocalDate transactionDate;
    private String lineType;
    private String documentNumber;
    private String description;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private BigDecimal runningBalance;
    private UUID referenceId;
}
