package com.finance.transactional.service;

import com.finance.transactional.client.CoreFinancePeriodClient;
import com.finance.transactional.dto.corefinance.AccountingPeriodLookupDto;
import com.finance.transactional.exception.ClosedAccountingPeriodException;
import com.finance.transactional.model.ap.Invoice;
import feign.FeignException;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceAccountingPeriodService {

    private final CoreFinancePeriodClient coreFinancePeriodClient;

    /**
     * Resolves the Core-Finance accounting period for the invoice date and blocks closed periods.
     * Persists period and fiscal year references on the invoice.
     */
    public void validateOpenPeriodForDate(UUID tenantId, LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date is required for accounting period validation");
        }
        AccountingPeriodLookupDto period = lookupPeriodForDate(date, tenantId);
        if (period == null || period.getId() == null) {
            throw new IllegalStateException("No accounting period found for date " + date);
        }
        if (isPeriodClosed(period)) {
            throw new ClosedAccountingPeriodException();
        }
    }

    public void resolveAndValidateOpenPeriod(Invoice invoice) {
        if (invoice.getInvoiceDate() == null) {
            throw new IllegalArgumentException("Invoice date is required");
        }

        AccountingPeriodLookupDto period = lookupPeriodForDate(invoice.getInvoiceDate(), invoice.getTenantId());
        if (period == null || period.getId() == null) {
            throw new IllegalStateException(
                    "No accounting period found for invoice date " + invoice.getInvoiceDate());
        }

        if (isPeriodClosed(period)) {
            throw new ClosedAccountingPeriodException();
        }

        invoice.setGlAccountingPeriodId(period.getId());
        invoice.setGlFiscalYearId(period.getFiscalYearId());
        log.debug(
                "Invoice {} linked to period {} (fiscal year {})",
                invoice.getInvoiceNumber(),
                period.getPeriodName(),
                period.getFiscalYearId());
    }

    private AccountingPeriodLookupDto lookupPeriodForDate(LocalDate invoiceDate, UUID tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant id is required for accounting period validation");
        }
        try {
            return coreFinancePeriodClient.getAccountingPeriodForDate(
                    invoiceDate.toString(), tenantId.toString());
        } catch (FeignException.NotFound ex) {
            return null;
        } catch (FeignException.Unauthorized | FeignException.Forbidden ex) {
            log.error("Core-Finance period lookup unauthorized for tenant {} date {}", tenantId, invoiceDate, ex);
            throw new IllegalStateException(
                    "Unable to validate accounting period with Core-Finance (authentication failed). "
                            + "Ensure your session token is valid and Core-Finance is running on "
                            + "CORE_FINANCE_URL (expected http://localhost:8084 for local Docker).",
                    ex);
        } catch (FeignException ex) {
            log.error(
                    "Core-Finance period lookup failed for tenant {} date {}: HTTP {} {}",
                    tenantId,
                    invoiceDate,
                    ex.status(),
                    ex.getMessage());
            throw new IllegalStateException(
                    "Unable to validate accounting period with Core-Finance (HTTP "
                            + ex.status()
                            + "). Ensure Core-Finance is running at CORE_FINANCE_URL "
                            + "(http://localhost:8084 locally) and the invoice date falls in a defined period.",
                    ex);
        }
    }

    private boolean isPeriodClosed(AccountingPeriodLookupDto period) {
        if (Boolean.TRUE.equals(period.getIsClosed())) {
            return true;
        }
        return !Boolean.TRUE.equals(period.getIsOpen());
    }
}
