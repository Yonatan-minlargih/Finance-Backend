package com.example.payroll_service.enums;

/**
 * Resource names for permission-based access control
 * Matches employee-service resource naming pattern
 */
public enum ResourceName {
    
    // Product resources
    PRODUCT_CREATE("product:create"),
    PRODUCT_READ("product:read"),
    PRODUCT_UPDATE("product:update"),
    PRODUCT_DELETE("product:delete"),
    PRODUCT_READ_ALL("product:read:all"),
    
    // ProfitCenter resources
    PROFIT_CENTER_CREATE("profit_center:create"),
    PROFIT_CENTER_READ("profit_center:read"),
    PROFIT_CENTER_UPDATE("profit_center:update"),
    PROFIT_CENTER_DELETE("profit_center:delete"),
    PROFIT_CENTER_READ_ALL("profit_center:read:all"),
    
    // CostCenter resources
    COST_CENTER_CREATE("cost_center:create"),
    COST_CENTER_READ("cost_center:read"),
    COST_CENTER_UPDATE("cost_center:update"),
    COST_CENTER_DELETE("cost_center:delete"),
    COST_CENTER_READ_ALL("cost_center:read:all"),
    
    // CostRecord resources
    COST_RECORD_CREATE("cost_record:create"),
    COST_RECORD_READ("cost_record:read"),
    COST_RECORD_UPDATE("cost_record:update"),
    COST_RECORD_DELETE("cost_record:delete"),
    COST_RECORD_READ_ALL("cost_record:read:all"),
    
    // StandardCostRate resources
    STANDARD_COST_RATE_CREATE("standard_cost_rate:create"),
    STANDARD_COST_RATE_READ("standard_cost_rate:read"),
    STANDARD_COST_RATE_UPDATE("standard_cost_rate:update"),
    STANDARD_COST_RATE_DELETE("standard_cost_rate:delete"),
    STANDARD_COST_RATE_READ_ALL("standard_cost_rate:read:all"),
    
    // CogsFormula resources
    COGS_FORMULA_CREATE("cogs_formula:create"),
    COGS_FORMULA_READ("cogs_formula:read"),
    COGS_FORMULA_UPDATE("cogs_formula:update"),
    COGS_FORMULA_DELETE("cogs_formula:delete"),
    COGS_FORMULA_READ_ALL("cogs_formula:read:all"),
    
    // ProfitabilityAnalysis resources
    PROFITABILITY_ANALYSIS_CREATE("profitability_analysis:create"),
    PROFITABILITY_ANALYSIS_READ("profitability_analysis:read"),
    PROFITABILITY_ANALYSIS_UPDATE("profitability_analysis:update"),
    PROFITABILITY_ANALYSIS_DELETE("profitability_analysis:delete"),
    PROFITABILITY_ANALYSIS_READ_ALL("profitability_analysis:read:all"),
    
    // WithholdingTaxRule resources
    WITHHOLDING_TAX_RULE_CREATE("withholding_tax_rule:create"),
    WITHHOLDING_TAX_RULE_READ("withholding_tax_rule:read"),
    WITHHOLDING_TAX_RULE_UPDATE("withholding_tax_rule:update"),
    WITHHOLDING_TAX_RULE_DELETE("withholding_tax_rule:delete"),
    WITHHOLDING_TAX_RULE_READ_ALL("withholding_tax_rule:read:all"),

    // PayrollRun resources
    PAYROLL_RUN_CREATE("payroll_run:create"),
    PAYROLL_RUN_READ("payroll_run:read"),
    PAYROLL_RUN_UPDATE("payroll_run:update"),
    PAYROLL_RUN_DELETE("payroll_run:delete"),
    PAYROLL_RUN_READ_ALL("payroll_run:read:all"),
    PAYROLL_RUN_PROCESS("payroll_run:process"),
    PAYROLL_RUN_APPROVE("payroll_run:approve"),
    PAYROLL_RUN_POST("payroll_run:post"),
    PAYROLL_RUN_CANCEL("payroll_run:cancel"),

    // PayrollDetail resources
    PAYROLL_DETAIL_READ("payroll_detail:read"),
    PAYROLL_DETAIL_READ_ALL("payroll_detail:read:all"),

    // Loan resources
    LOAN_CREATE("loan:create"),
    LOAN_READ("loan:read"),
    LOAN_UPDATE("loan:update"),
    LOAN_DELETE("loan:delete"),
    LOAN_READ_ALL("loan:read:all"),
    LOAN_STOP("loan:stop"),
    LOAN_RESUME("loan:resume"),

    // LoanPayment resources
    LOAN_PAYMENT_READ("loan_payment:read"),
    LOAN_PAYMENT_READ_ALL("loan_payment:read:all"),

    // SalaryComponent resources
    SALARY_COMPONENT_CREATE("salary_component:create"),
    SALARY_COMPONENT_READ("salary_component:read"),
    SALARY_COMPONENT_UPDATE("salary_component:update"),
    SALARY_COMPONENT_DELETE("salary_component:delete"),
    SALARY_COMPONENT_READ_ALL("salary_component:read:all"),

    // EarningDeductionType resources
    EARNING_DEDUCTION_TYPE_CREATE("earning_deduction_type:create"),
    EARNING_DEDUCTION_TYPE_READ("earning_deduction_type:read"),
    EARNING_DEDUCTION_TYPE_UPDATE("earning_deduction_type:update"),
    EARNING_DEDUCTION_TYPE_DELETE("earning_deduction_type:delete"),
    EARNING_DEDUCTION_TYPE_READ_ALL("earning_deduction_type:read:all");
    
    private final String permission;
    
    ResourceName(String permission) {
        this.permission = permission;
    }
    
    public String getPermission() {
        return permission;
    }
    
    @Override
    public String toString() {
        return permission;
    }
}
