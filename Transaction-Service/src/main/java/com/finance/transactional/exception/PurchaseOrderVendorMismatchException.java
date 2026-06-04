package com.finance.transactional.exception;

public class PurchaseOrderVendorMismatchException extends IllegalArgumentException {

    public static final String MESSAGE = "Vendor must match the purchase order vendor";

    public PurchaseOrderVendorMismatchException() {
        super(MESSAGE);
    }
}
