package com.finance.transactional.exception;

public class PurchaseOrderInvoicedAmountExceededException extends IllegalStateException {

    public static final String MESSAGE = "Total invoiced amount would exceed purchase order amount";

    public PurchaseOrderInvoicedAmountExceededException(String detail) {
        super(detail != null && !detail.isBlank() ? detail : MESSAGE);
    }
}
