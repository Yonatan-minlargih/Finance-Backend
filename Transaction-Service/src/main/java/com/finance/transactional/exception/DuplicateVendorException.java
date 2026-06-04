package com.finance.transactional.exception;

public class DuplicateVendorException extends RuntimeException {

    public static final String MESSAGE = "Vendor already exists";

    public DuplicateVendorException() {
        super(MESSAGE);
    }
}
