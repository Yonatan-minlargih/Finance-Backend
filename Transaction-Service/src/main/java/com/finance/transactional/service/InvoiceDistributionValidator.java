package com.finance.transactional.service;

import com.finance.transactional.model.ap.Invoice;
import com.finance.transactional.model.ap.InvoiceLine;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InvoiceDistributionValidator {

    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public void validateAndNormalize(Invoice invoice) {
        if (invoice.getLines() == null || invoice.getLines().isEmpty()) {
            if (invoice.getTotalAmount() != null
                    && invoice.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalArgumentException(
                        "Invoice total is greater than zero; add at least one distribution line.");
            }
            return;
        }

        BigDecimal tax = invoice.getTaxAmount() != null ? invoice.getTaxAmount() : BigDecimal.ZERO;
        BigDecimal subtotal = sumLineAmounts(invoice.getLines());
        invoice.setSubtotalAmount(subtotal);

        normalizeLineAmountsFromPercentages(invoice.getLines(), subtotal);

        subtotal = sumLineAmounts(invoice.getLines());
        invoice.setSubtotalAmount(subtotal);

        BigDecimal expectedPayable = subtotal.add(tax);
        if (invoice.getTotalAmount() == null || invoice.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setTotalAmount(expectedPayable);
        } else if (expectedPayable.subtract(invoice.getTotalAmount()).abs().compareTo(TOLERANCE) > 0) {
            throw new IllegalArgumentException(String.format(
                    "Invoice total must equal subtotal plus VAT (subtotal: %s, VAT: %s, expected total: %s, actual: %s).",
                    subtotal.toPlainString(),
                    tax.toPlainString(),
                    expectedPayable.toPlainString(),
                    invoice.getTotalAmount().toPlainString()));
        }

        if (subtotal.compareTo(BigDecimal.ZERO) > 0
                && tax.compareTo(BigDecimal.ZERO) > 0
                && invoice.getVatRate() != null
                && invoice.getVatRate().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal impliedVat = subtotal
                    .multiply(invoice.getVatRate())
                    .divide(HUNDRED, 2, RoundingMode.HALF_UP);
            if (impliedVat.subtract(tax).abs().compareTo(TOLERANCE) > 0) {
                throw new IllegalArgumentException(String.format(
                        "VAT amount %s does not match subtotal × rate (%s%% → %s).",
                        tax.toPlainString(),
                        invoice.getVatRate().stripTrailingZeros().toPlainString(),
                        impliedVat.toPlainString()));
            }
        }

        validatePercentages(invoice.getLines());
    }

    private void normalizeLineAmountsFromPercentages(List<InvoiceLine> lines, BigDecimal subtotal) {
        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        for (InvoiceLine line : lines) {
            if (line.getAllocationPercentage() != null
                    && line.getAllocationPercentage().compareTo(BigDecimal.ZERO) > 0) {
                line.setLineAmount(subtotal
                        .multiply(line.getAllocationPercentage())
                        .divide(HUNDRED, 2, RoundingMode.HALF_UP));
            }
        }
    }

    private void validatePercentages(List<InvoiceLine> lines) {
        boolean anyPercentage = lines.stream().anyMatch(line -> line.getAllocationPercentage() != null);
        if (!anyPercentage) {
            return;
        }

        BigDecimal pctSum = lines.stream()
                .map(line -> line.getAllocationPercentage() != null
                        ? line.getAllocationPercentage()
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (pctSum.subtract(HUNDRED).abs().compareTo(TOLERANCE) > 0) {
            throw new IllegalArgumentException(String.format(
                    "Allocation percentages must total 100%% (current total: %s%%).",
                    pctSum.stripTrailingZeros().toPlainString()));
        }

        for (InvoiceLine line : lines) {
            if (line.getLineAmount() == null || line.getLineAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Each distribution line must have an amount greater than zero.");
            }
            if (line.getAccountId() == null || line.getAccountId().isBlank()) {
                throw new IllegalArgumentException("Each distribution line must have a GL account.");
            }
        }
    }

    private BigDecimal sumLineAmounts(List<InvoiceLine> lines) {
        return lines.stream()
                .map(line -> line.getLineAmount() != null ? line.getLineAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
