package com.finance.transactional.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApVendorStatementDto {
    private UUID vendorId;
    private String vendorName;
    private String vendorCode;
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalDate asOfDate;
    private BigDecimal openingBalance;
    private BigDecimal totalInvoices;
    private BigDecimal totalPayments;
    private BigDecimal totalCredits;
    private BigDecimal closingOutstandingBalance;
    private List<ApVendorStatementLineDto> lines;
}
