package com.finance.transactional.client;

import com.finance.transactional.model.ap.Invoice;

public interface CoreFinanceClient {
    void postInvoiceApprovalJournal(Invoice invoice);
}
