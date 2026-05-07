package com.financial.corefinance.exception;

public class BudgetValidationException extends RuntimeException {
    
    public BudgetValidationException(String message) {
        super(message);
    }
    
    public BudgetValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
