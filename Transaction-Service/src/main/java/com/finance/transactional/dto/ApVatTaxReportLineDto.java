package com.finance.transactional.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApVatTaxReportLineDto {

    private UUID invoiceId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private String vendorName;
    private String vendorTaxId;
    private String vendorVatNumber;
    private BigDecimal subtotalAmount;
    private BigDecimal vatRate;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String currency;
    private String glJournalNumber;
    private UUID glAccountingPeriodId;
}
