package com.example.payroll_service.exception;

public class InvalidPayrollStateException extends RuntimeException {

    public InvalidPayrollStateException(String message) {
        super(message);
    }

    public InvalidPayrollStateException(String currentState, String expectedState) {
        super(String.format("Invalid payroll state. Current: %s, Expected: %s", currentState, expectedState));
    }
}
