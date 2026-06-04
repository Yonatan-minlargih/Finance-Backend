package com.finance.transactional.service;

import com.finance.transactional.dto.ApAgingReportDto;
import com.finance.transactional.dto.ApAgingReportLineDto;
import com.finance.transactional.model.ap.Invoice;
import com.finance.transactional.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApAgingReportService {

    private final InvoiceRepository invoiceRepository;
    private final ApSubledgerBalanceService subledgerBalanceService;

    @Transactional(readOnly = true)
    public ApAgingReportDto buildAgingReport(UUID tenantId, LocalDate asOfDate) {
        LocalDate effectiveAsOf = asOfDate != null ? asOfDate : LocalDate.now();
        List<Invoice> openInvoices = invoiceRepository.findOpenPayablesForAging(tenantId);

        Map<String, BigDecimal> buckets = new LinkedHashMap<>();
        buckets.put("Current", BigDecimal.ZERO);
        buckets.put("1-30 Days", BigDecimal.ZERO);
        buckets.put("31-60 Days", BigDecimal.ZERO);
        buckets.put("61-90 Days", BigDecimal.ZERO);
        buckets.put("91-120 Days", BigDecimal.ZERO);
        buckets.put("Over 120 Days", BigDecimal.ZERO);

        List<ApAgingReportLineDto> lines = new ArrayList<>();
        BigDecimal totalOutstanding = BigDecimal.ZERO;

        for (Invoice invoice : openInvoices) {
            BigDecimal outstanding = subledgerBalanceService.computeOutstanding(tenantId, invoice);
            if (outstanding.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            LocalDate ageFrom = invoice.getInvoiceDate() != null ? invoice.getInvoiceDate() : effectiveAsOf;
            long daysOld = ChronoUnit.DAYS.between(ageFrom, effectiveAsOf);
            String bucket = resolveBucket(daysOld);

            if (invoice.getInvoiceType() == Invoice.InvoiceType.CREDIT_MEMO) {
                buckets.put(bucket, buckets.get(bucket).subtract(outstanding));
                totalOutstanding = totalOutstanding.subtract(outstanding);
            } else {
                buckets.put(bucket, buckets.get(bucket).add(outstanding));
                totalOutstanding = totalOutstanding.add(outstanding);
            }

            lines.add(ApAgingReportLineDto.builder()
                    .invoiceId(invoice.getId())
                    .invoiceNumber(invoice.getInvoiceNumber())
                    .vendorId(invoice.getVendor() != null ? invoice.getVendor().getId() : null)
                    .vendorName(invoice.getVendor() != null ? invoice.getVendor().getVendorName() : null)
                    .invoiceDate(invoice.getInvoiceDate())
                    .dueDate(invoice.getDueDate())
                    .totalAmount(invoice.getTotalAmount())
                    .outstandingAmount(outstanding)
                    .daysOld(daysOld)
                    .agingBucket(bucket)
                    .currency(invoice.getCurrency())
                    .status(invoice.getStatus() != null ? invoice.getStatus().name() : null)
                    .build());
        }

        return ApAgingReportDto.builder()
                .asOfDate(effectiveAsOf)
                .buckets(buckets)
                .totalOutstanding(totalOutstanding)
                .lines(lines)
                .build();
    }

    static String resolveBucket(long daysOld) {
        if (daysOld <= 0) {
            return "Current";
        }
        if (daysOld <= 30) {
            return "1-30 Days";
        }
        if (daysOld <= 60) {
            return "31-60 Days";
        }
        if (daysOld <= 90) {
            return "61-90 Days";
        }
        if (daysOld <= 120) {
            return "91-120 Days";
        }
        return "Over 120 Days";
    }
}
