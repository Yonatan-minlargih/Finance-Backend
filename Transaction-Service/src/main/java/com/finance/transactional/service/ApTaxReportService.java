package com.finance.transactional.service;

import com.finance.transactional.dto.ApVatTaxReportLineDto;
import com.finance.transactional.model.ap.Invoice;
import com.finance.transactional.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApTaxReportService {

    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public List<ApVatTaxReportLineDto> vatInputReport(UUID tenantId, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("fromDate and toDate are required");
        }
        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("toDate must be on or after fromDate");
        }

        return invoiceRepository.findTaxableInvoicesForPeriod(tenantId, fromDate, toDate).stream()
                .map(this::toReportLine)
                .toList();
    }

    private ApVatTaxReportLineDto toReportLine(Invoice invoice) {
        String vendorName = invoice.getVendor() != null ? invoice.getVendor().getVendorName() : null;
        return ApVatTaxReportLineDto.builder()
                .invoiceId(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceDate(invoice.getInvoiceDate())
                .vendorName(vendorName)
                .vendorTaxId(invoice.getVendorTaxId())
                .vendorVatNumber(invoice.getVendorVatNumber())
                .subtotalAmount(invoice.getSubtotalAmount())
                .vatRate(invoice.getVatRate())
                .taxAmount(invoice.getTaxAmount())
                .totalAmount(invoice.getTotalAmount())
                .currency(invoice.getCurrency())
                .glJournalNumber(invoice.getGlJournalNumber())
                .glAccountingPeriodId(invoice.getGlAccountingPeriodId())
                .build();
    }
}
