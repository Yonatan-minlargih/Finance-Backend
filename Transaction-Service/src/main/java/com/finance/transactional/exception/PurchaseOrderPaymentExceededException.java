package com.finance.transactional.exception;

public class PurchaseOrderPaymentExceededException extends IllegalStateException {

    public static final String MESSAGE = "Total payments would exceed purchase order amount";

    public PurchaseOrderPaymentExceededException(String detail) {
        super(detail != null && !detail.isBlank() ? detail : MESSAGE);
    }
}
