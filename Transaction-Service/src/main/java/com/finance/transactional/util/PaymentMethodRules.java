package com.finance.transactional.util;

import java.util.Locale;

public final class PaymentMethodRules {

    public static final String BANK_TRANSFER = "Bank Transfer";
    public static final String CHEQUE = "Cheque";
    public static final String CASH = "Cash";

    private PaymentMethodRules() {}

    public static boolean requiresBankAccount(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return true;
        }
        String normalized = paymentMethod.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        return normalized.contains("BANK") || normalized.equals("WIRE") || normalized.equals("BANK_TRANSFER");
    }

    public static String normalize(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return BANK_TRANSFER;
        }
        String n = paymentMethod.trim().toUpperCase(Locale.ROOT);
        if (n.contains("BANK") || n.equals("WIRE")) {
            return BANK_TRANSFER;
        }
        if (n.contains("CHEQUE") || n.contains("CHECK")) {
            return CHEQUE;
        }
        if (n.contains("CASH")) {
            return CASH;
        }
        return paymentMethod.trim();
    }
}
