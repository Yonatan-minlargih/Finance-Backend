package com.finance.transactional.exception;

public class DuplicateInvoiceException extends RuntimeException {

    public static final String MESSAGE =
            "An invoice with this number already exists for this vendor. Use a different invoice number or vendor.";

    public DuplicateInvoiceException() {
        super(MESSAGE);
    }

    public DuplicateInvoiceException(String invoiceNumber, String vendorName) {
        super("Invoice " + invoiceNumber + " already exists for vendor " + vendorName);
    }
}
