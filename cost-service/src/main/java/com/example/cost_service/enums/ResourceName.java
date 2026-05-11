package com.example.cost_service.enums;

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
    WITHHOLDING_TAX_RULE_READ_ALL("withholding_tax_rule:read:all");
    
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
