package com.finance.transactional.utility;
import com.finance.transactional.model.ap.Invoice;
import com.finance.transactional.model.ap.InvoiceLine;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CoreFinanceJournalPayloadFactory {

    public Map<String, Object> fromInvoiceApproval(Invoice invoice) {
        Map<String, Object> journalPayload = new HashMap<>();
        journalPayload.put("journal_date", invoice.getInvoiceDate());
        journalPayload.put("description", "AP Invoice Approval: " + invoice.getInvoiceNumber() + " - " + invoice.getVendor().getVendorName());
        journalPayload.put("journal_type", "AP_INVOICE");
        journalPayload.put("reference_type", "INVOICE");
        journalPayload.put("reference_number", invoice.getInvoiceNumber());
        journalPayload.put("source_module", "TRANSACTIONAL_AP");
        journalPayload.put("source_id", invoice.getId().toString());
        journalPayload.put("currency_code", invoice.getCurrency());

        List<Map<String, Object>> lines = new ArrayList<>();
        int lineNum = 1;

        // 1. Debit Expense Accounts (from Invoice Lines)
        for (InvoiceLine invLine : invoice.getLines()) {
            Map<String, Object> debitLine = new HashMap<>();
            debitLine.put("line_number", lineNum++);
            debitLine.put("account_id", invLine.getAccountId());
            debitLine.put("debit_amount", invLine.getLineAmount());
            debitLine.put("credit_amount", BigDecimal.ZERO);
            debitLine.put("description", invLine.getDescription());
            lines.add(debitLine);
        }

        // 2. Credit Accounts Payable Liability Account
        // Note: In a full system, this would be looked up from vendor configuration or a system setup
        Map<String, Object> creditLine = new HashMap<>();
        creditLine.put("line_number", lineNum++);
        creditLine.put("account_id", "2100"); // Default AP Liability Account
        creditLine.put("debit_amount", BigDecimal.ZERO);
        creditLine.put("credit_amount", invoice.getTotalAmount());
        creditLine.put("description", "AP Liability - " + invoice.getVendor().getVendorName());
        lines.add(creditLine);

        journalPayload.put("journal_lines", lines);
        return journalPayload;
    }
}
