package com.finance.transactional.exception;

public class ApInvoiceApprovalException extends RuntimeException {

    public ApInvoiceApprovalException(String message) {
        super(message);
    }

    public ApInvoiceApprovalException(String message, Throwable cause) {
        super(message, cause);
    }
}
