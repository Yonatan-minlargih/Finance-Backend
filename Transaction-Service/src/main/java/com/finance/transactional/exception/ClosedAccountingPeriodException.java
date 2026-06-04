package com.finance.transactional.exception;

public class ClosedAccountingPeriodException extends IllegalStateException {

    public static final String MESSAGE = "Accounting period is closed";

    public ClosedAccountingPeriodException() {
        super(MESSAGE);
    }

    public ClosedAccountingPeriodException(String detail) {
        super(detail != null && !detail.isBlank() ? detail : MESSAGE);
    }
}
