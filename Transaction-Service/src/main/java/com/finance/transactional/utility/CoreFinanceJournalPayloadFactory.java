package com.finance.transactional.utility;

import com.finance.transactional.model.ap.Invoice;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CoreFinanceJournalPayloadFactory {

    public Map<String, Object> fromInvoiceApproval(Invoice invoice) {
        Map<String, Object> journalPayload = new HashMap<>();
        journalPayload.put("journal_date", invoice.getInvoiceDate());
        journalPayload.put("description", "AP Invoice Approval: " + invoice.getInvoiceNumber());
        journalPayload.put("journal_type", "AP_INVOICE");
        journalPayload.put("reference_type", "INVOICE");
        journalPayload.put("reference_number", invoice.getInvoiceNumber());
        journalPayload.put("source_module", "TRANSACTIONAL_AP");
        journalPayload.put("source_id", invoice.getId());
        journalPayload.put("total_amount", invoice.getTotalAmount());
        return journalPayload;
    }
}
