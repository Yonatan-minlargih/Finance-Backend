package com.financial.corefinance.integration;

/**
 * Keys matching {@code integration.gl-accounts.accounts} in {@code integration-gl-accounts.yml}.
 */
public final class IntegrationAccountKeys {

    public static final String AP_PAYABLE = "ap-payable";
    public static final String AP_EXPENSE_DEFAULT = "ap-expense-default";
    public static final String AP_EXPENSE_ALTERNATE = "ap-expense-alternate";
    public static final String AP_VAT_INPUT = "ap-vat-input";
    public static final String AP_BANK = "ap-bank";
    public static final String AR_RECEIVABLE = "ar-receivable";
    public static final String AR_REVENUE = "ar-revenue";
    public static final String AR_BAD_DEBT_EXPENSE = "ar-bad-debt-expense";
    public static final String AR_INTEREST_INCOME = "ar-interest-income";
    public static final String PAYROLL_DEDUCTIONS = "payroll-deductions";

    private IntegrationAccountKeys() {}
}
