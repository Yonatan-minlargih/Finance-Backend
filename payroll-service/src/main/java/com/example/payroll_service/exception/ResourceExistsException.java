package com.example.payroll_service.exception;

public class ResourceExistsException extends RuntimeException {

    public ResourceExistsException(String message) {
        super(message);
    }

    public ResourceExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
