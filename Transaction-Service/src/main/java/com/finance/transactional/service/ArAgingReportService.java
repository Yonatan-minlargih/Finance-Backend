package com.finance.transactional.service;

import com.finance.transactional.config.ArAgingProperties;
import com.finance.transactional.dto.ArAgingReportDto;
import com.finance.transactional.dto.ArAgingReportLineDto;
import com.finance.transactional.model.ar.SalesInvoice;
import com.finance.transactional.repository.SalesInvoiceRepository;
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
public class ArAgingReportService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final ArSubledgerBalanceService subledgerBalanceService;
    private final ArAgingProperties arAgingProperties;

    @Transactional(readOnly = true)
    public ArAgingReportDto buildReport(UUID tenantId, LocalDate asOfDate, String customBucketDays) {
        LocalDate effectiveAsOf = asOfDate != null ? asOfDate : LocalDate.now();
        List<Integer> boundaries = arAgingProperties.parseBucketDays(customBucketDays);
        List<String> bucketLabels = buildBucketLabels(boundaries);

        Map<String, BigDecimal> buckets = new LinkedHashMap<>();
        for (String label : bucketLabels) {
            buckets.put(label, BigDecimal.ZERO);
        }

        List<ArAgingReportLineDto> lines = new ArrayList<>();
        BigDecimal totalOutstanding = BigDecimal.ZERO;

        for (SalesInvoice invoice : salesInvoiceRepository.findOpenReceivablesForAging(tenantId)) {
            BigDecimal outstanding = subledgerBalanceService.computeOutstanding(tenantId, invoice, null);
            if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            LocalDate ageFrom = invoice.getInvoiceDate() != null ? invoice.getInvoiceDate() : effectiveAsOf;
            long daysOld = ChronoUnit.DAYS.between(ageFrom, effectiveAsOf);
            String bucket = resolveBucket(daysOld, boundaries, bucketLabels);

            buckets.put(bucket, buckets.get(bucket).add(outstanding));
            totalOutstanding = totalOutstanding.add(outstanding);

            lines.add(ArAgingReportLineDto.builder()
                    .invoiceId(invoice.getId())
                    .invoiceNumber(invoice.getInvoiceNumber())
                    .customerId(invoice.getCustomer() != null ? invoice.getCustomer().getId() : null)
                    .customerName(invoice.getCustomer() != null ? invoice.getCustomer().getCustomerName() : null)
                    .invoiceDate(invoice.getInvoiceDate())
                    .dueDate(invoice.getDueDate())
                    .totalAmount(invoice.getTotalAmount())
                    .outstandingAmount(outstanding)
                    .daysOld(daysOld)
                    .agingBucket(bucket)
                    .status(invoice.getStatus() != null ? invoice.getStatus().name() : null)
                    .build());
        }

        return ArAgingReportDto.builder()
                .asOfDate(effectiveAsOf)
                .bucketBoundaries(boundaries)
                .buckets(buckets)
                .totalOutstanding(totalOutstanding)
                .lines(lines)
                .build();
    }

    static List<String> buildBucketLabels(List<Integer> boundaries) {
        List<String> labels = new ArrayList<>();
        labels.add("Current");
        int prev = 0;
        for (Integer bound : boundaries) {
            if (prev == 0) {
                labels.add("1-" + bound + " Days");
            } else {
                labels.add((prev + 1) + "-" + bound + " Days");
            }
            prev = bound;
        }
        labels.add("Over " + prev + " Days");
        return labels;
    }

    static String resolveBucket(long daysOld, List<Integer> boundaries, List<String> labels) {
        if (daysOld <= 0) {
            return labels.get(0);
        }
        int prev = 0;
        for (int i = 0; i < boundaries.size(); i++) {
            int bound = boundaries.get(i);
            if (daysOld <= bound) {
                return labels.get(i + 1);
            }
            prev = bound;
        }
        return labels.get(labels.size() - 1);
    }
}
