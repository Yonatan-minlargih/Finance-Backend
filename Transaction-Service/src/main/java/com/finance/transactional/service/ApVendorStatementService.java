package com.finance.transactional.service;

import com.finance.transactional.dto.ApVendorStatementDto;
import com.finance.transactional.dto.ApVendorStatementLineDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.ap.Invoice;
import com.finance.transactional.model.ap.Payment;
import com.finance.transactional.model.ap.Vendor;
import com.finance.transactional.repository.InvoiceRepository;
import com.finance.transactional.repository.PaymentRepository;
import com.finance.transactional.repository.VendorRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApVendorStatementService {

    private final VendorRepository vendorRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ApSubledgerBalanceService subledgerBalanceService;

    @Transactional(readOnly = true)
    public ApVendorStatementDto buildStatement(
            UUID tenantId, UUID vendorId, LocalDate fromDate, LocalDate toDate, LocalDate asOfDate) {
        Vendor vendor = vendorRepository
                .findByTenantIdAndId(tenantId, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id " + vendorId));

        LocalDate effectiveTo = toDate != null ? toDate : LocalDate.now();
        LocalDate effectiveFrom = fromDate != null ? fromDate : effectiveTo.minusMonths(12);
        LocalDate effectiveAsOf = asOfDate != null ? asOfDate : effectiveTo;

        List<ApVendorStatementLineDto> rawLines = new ArrayList<>();

        List<Invoice> invoices = invoiceRepository.findVendorInvoicesThroughDate(tenantId, vendorId, effectiveTo);
        for (Invoice invoice : invoices) {
            if (invoice.getInvoiceDate() == null
                    || invoice.getInvoiceDate().isBefore(effectiveFrom)
                    || invoice.getInvoiceDate().isAfter(effectiveTo)) {
                continue;
            }
            if (invoice.getStatus() == Invoice.InvoiceStatus.DRAFT
                    || invoice.getStatus() == Invoice.InvoiceStatus.PENDING_APPROVAL
                    || invoice.getStatus() == Invoice.InvoiceStatus.REJECTED) {
                continue;
            }

            BigDecimal amount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
            boolean credit = invoice.getInvoiceType() == Invoice.InvoiceType.CREDIT_MEMO
                    || invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED;

            if (credit) {
                rawLines.add(line(
                        invoice.getInvoiceDate(),
                        "CREDIT",
                        invoice.getInvoiceNumber(),
                        creditDescription(invoice),
                        BigDecimal.ZERO,
                        amount,
                        invoice.getId()));
            } else if (invoice.getStatus() != Invoice.InvoiceStatus.CANCELLED) {
                rawLines.add(line(
                        invoice.getInvoiceDate(),
                        "INVOICE",
                        invoice.getInvoiceNumber(),
                        "AP Invoice — " + invoice.getInvoiceNumber(),
                        amount,
                        BigDecimal.ZERO,
                        invoice.getId()));
            }
        }

        List<Payment> payments = paymentRepository.findByVendorAndDateRange(
                tenantId, vendorId, effectiveFrom, effectiveTo);
        for (Payment payment : payments) {
            BigDecimal amount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
            rawLines.add(line(
                    payment.getPaymentDate(),
                    "PAYMENT",
                    payment.getPaymentNumber(),
                    "Payment — " + (payment.getReferenceNumber() != null ? payment.getReferenceNumber() : payment.getPaymentNumber()),
                    BigDecimal.ZERO,
                    amount,
                    payment.getId()));
        }

        rawLines.sort(Comparator.comparing(ApVendorStatementLineDto::getTransactionDate)
                .thenComparing(ApVendorStatementLineDto::getDocumentNumber, Comparator.nullsLast(String::compareTo)));

        BigDecimal openingBalance = computeOpeningBalance(tenantId, vendorId, effectiveFrom);
        BigDecimal running = openingBalance;
        BigDecimal totalInvoices = BigDecimal.ZERO;
        BigDecimal totalPayments = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (ApVendorStatementLineDto row : rawLines) {
            running = running.add(row.getDebitAmount()).subtract(row.getCreditAmount());
            row.setRunningBalance(running);
            if ("INVOICE".equals(row.getLineType())) {
                totalInvoices = totalInvoices.add(row.getDebitAmount());
            } else if ("PAYMENT".equals(row.getLineType())) {
                totalPayments = totalPayments.add(row.getCreditAmount());
            } else if ("CREDIT".equals(row.getLineType())) {
                totalCredits = totalCredits.add(row.getCreditAmount());
            }
        }

        BigDecimal closingOutstanding = computeClosingOutstanding(tenantId, vendorId, effectiveAsOf);

        return ApVendorStatementDto.builder()
                .vendorId(vendorId)
                .vendorName(vendor.getVendorName())
                .vendorCode(vendor.getVendorCode())
                .fromDate(effectiveFrom)
                .toDate(effectiveTo)
                .asOfDate(effectiveAsOf)
                .openingBalance(openingBalance)
                .totalInvoices(totalInvoices)
                .totalPayments(totalPayments)
                .totalCredits(totalCredits)
                .closingOutstandingBalance(closingOutstanding)
                .lines(rawLines)
                .build();
    }

    private BigDecimal computeOpeningBalance(UUID tenantId, UUID vendorId, LocalDate fromDate) {
        BigDecimal balance = BigDecimal.ZERO;
        List<Invoice> priorInvoices = invoiceRepository.findVendorInvoicesThroughDate(
                tenantId, vendorId, fromDate.minusDays(1));
        for (Invoice invoice : priorInvoices) {
            if (invoice.getInvoiceDate() != null && !invoice.getInvoiceDate().isBefore(fromDate)) {
                continue;
            }
            balance = balance.add(signedInvoiceImpact(tenantId, invoice));
        }
        List<Payment> priorPayments = paymentRepository.findByVendorAndDateRange(
                tenantId, vendorId, LocalDate.of(1900, 1, 1), fromDate.minusDays(1));
        for (Payment payment : priorPayments) {
            BigDecimal amount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
            balance = balance.subtract(amount);
        }
        return balance;
    }

    private BigDecimal computeClosingOutstanding(UUID tenantId, UUID vendorId, LocalDate asOfDate) {
        BigDecimal total = BigDecimal.ZERO;
        List<Invoice> invoices = invoiceRepository.findVendorInvoicesThroughDate(tenantId, vendorId, asOfDate);
        for (Invoice invoice : invoices) {
            if (invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED
                    || invoice.getStatus() == Invoice.InvoiceStatus.REJECTED
                    || invoice.getStatus() == Invoice.InvoiceStatus.DRAFT
                    || invoice.getStatus() == Invoice.InvoiceStatus.PENDING_APPROVAL
                    || invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
                continue;
            }
            if (invoice.getInvoiceType() == Invoice.InvoiceType.CREDIT_MEMO) {
                BigDecimal credit = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
                total = total.subtract(credit);
            } else {
                total = total.add(subledgerBalanceService.computeOutstanding(tenantId, invoice));
            }
        }
        return total.max(BigDecimal.ZERO);
    }

    private BigDecimal signedInvoiceImpact(UUID tenantId, Invoice invoice) {
        if (invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED
                || invoice.getStatus() == Invoice.InvoiceStatus.REJECTED
                || invoice.getStatus() == Invoice.InvoiceStatus.DRAFT) {
            return BigDecimal.ZERO;
        }
        if (invoice.getInvoiceType() == Invoice.InvoiceType.CREDIT_MEMO) {
            BigDecimal amount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
            return amount.negate();
        }
        return subledgerBalanceService.computeOutstanding(tenantId, invoice);
    }

    private static String creditDescription(Invoice invoice) {
        if (invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED) {
            return "Void / reversal — " + invoice.getInvoiceNumber();
        }
        return "Credit memo — " + invoice.getInvoiceNumber();
    }

    private static ApVendorStatementLineDto line(
            LocalDate date,
            String type,
            String docNo,
            String description,
            BigDecimal debit,
            BigDecimal credit,
            UUID refId) {
        return ApVendorStatementLineDto.builder()
                .transactionDate(date)
                .lineType(type)
                .documentNumber(docNo)
                .description(description)
                .debitAmount(debit != null ? debit : BigDecimal.ZERO)
                .creditAmount(credit != null ? credit : BigDecimal.ZERO)
                .referenceId(refId)
                .build();
    }
}
