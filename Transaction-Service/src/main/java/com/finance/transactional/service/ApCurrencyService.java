package com.finance.transactional.service;

import com.finance.transactional.model.ap.Invoice;
import com.finance.transactional.model.ap.Payment;
import com.finance.transactional.model.ap.Vendor;
import com.finance.transactional.repository.VendorRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApCurrencyService {

    private static final int MONEY_SCALE = 2;
    private static final int RATE_SCALE = 8;

    private final VendorRepository vendorRepository;

    @Value("${ap.base-currency:ETB}")
    private String baseCurrency;

    public void applyVendorCurrencyToPayment(Payment payment, UUID tenantId, String currency, BigDecimal exchangeRate, BigDecimal foreignAmount) {
        Vendor vendor = loadVendor(tenantId, payment.getVendor() != null ? payment.getVendor().getId() : null);
        String txnCurrency = resolveCurrency(currency, vendor);
        payment.setCurrency(txnCurrency);

        BigDecimal rate = normalizeRate(exchangeRate, txnCurrency);
        payment.setExchangeRate(rate);

        BigDecimal foreign = foreignAmount != null ? foreignAmount : payment.getAmount();
        if (foreign == null) {
            throw new IllegalArgumentException("Payment amount is required");
        }
        payment.setForeignAmount(foreign.setScale(MONEY_SCALE, RoundingMode.HALF_UP));

        if (isForeignCurrency(txnCurrency)) {
            payment.setAmount(foreign.multiply(rate).setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        } else {
            payment.setAmount(foreign.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
            payment.setForeignAmount(foreign.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        }
    }

    public void applyVendorCurrencyToInvoice(
            Invoice invoice, UUID tenantId, String currency, BigDecimal exchangeRate, BigDecimal foreignTotalAmount) {
        Vendor vendor = loadVendor(tenantId, invoice.getVendor() != null ? invoice.getVendor().getId() : null);
        String txnCurrency = resolveCurrency(currency != null ? currency : invoice.getCurrency(), vendor);
        invoice.setCurrency(txnCurrency);

        BigDecimal rate = normalizeRate(exchangeRate, txnCurrency);
        invoice.setExchangeRate(rate);

        BigDecimal foreignTotal = foreignTotalAmount != null ? foreignTotalAmount : invoice.getTotalAmount();
        if (foreignTotal == null) {
            return;
        }
        foreignTotal = foreignTotal.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        invoice.setForeignTotalAmount(foreignTotal);

        if (isForeignCurrency(txnCurrency)) {
            invoice.setTotalAmount(foreignTotal.multiply(rate).setScale(MONEY_SCALE, RoundingMode.HALF_UP));
            if (invoice.getSubtotalAmount() != null && invoice.getTaxAmount() != null) {
                BigDecimal foreignSubtotal = invoice.getSubtotalAmount();
                invoice.setSubtotalAmount(foreignSubtotal.multiply(rate).setScale(MONEY_SCALE, RoundingMode.HALF_UP));
                invoice.setTaxAmount(invoice.getTaxAmount().multiply(rate).setScale(MONEY_SCALE, RoundingMode.HALF_UP));
            }
        } else {
            invoice.setTotalAmount(foreignTotal);
            invoice.setForeignTotalAmount(foreignTotal);
        }
    }

    public String getBaseCurrency() {
        return baseCurrency != null ? baseCurrency.trim().toUpperCase() : "ETB";
    }

    public boolean isForeignCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return false;
        }
        return !currency.trim().equalsIgnoreCase(getBaseCurrency());
    }

    private Vendor loadVendor(UUID tenantId, UUID vendorId) {
        if (vendorId == null) {
            return null;
        }
        return vendorRepository.findByTenantIdAndId(tenantId, vendorId).orElse(null);
    }

    private String resolveCurrency(String explicit, Vendor vendor) {
        if (explicit != null && !explicit.isBlank()) {
            return explicit.trim().toUpperCase();
        }
        if (vendor != null && vendor.getDefaultCurrency() != null && !vendor.getDefaultCurrency().isBlank()) {
            return vendor.getDefaultCurrency().trim().toUpperCase();
        }
        return getBaseCurrency();
    }

    private BigDecimal normalizeRate(BigDecimal exchangeRate, String txnCurrency) {
        if (!isForeignCurrency(txnCurrency)) {
            return BigDecimal.ONE;
        }
        if (exchangeRate == null || exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Exchange rate is required and must be greater than zero for currency " + txnCurrency);
        }
        return exchangeRate.setScale(RATE_SCALE, RoundingMode.HALF_UP);
    }
}
