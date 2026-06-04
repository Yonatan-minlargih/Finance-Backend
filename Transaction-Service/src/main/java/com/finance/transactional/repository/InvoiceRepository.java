package com.finance.transactional.repository;

import com.finance.transactional.model.ap.Invoice;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice>  {

    @EntityGraph(attributePaths = {"lines", "vendor", "purchaseOrder"})
    List<Invoice> findByTenantId(UUID tenantId);

    @EntityGraph(attributePaths = {"lines", "vendor", "purchaseOrder"})
    Optional<Invoice> findByTenantIdAndId(UUID tenantId, UUID id);

    boolean existsByTenantIdAndVendorIdAndInvoiceNumber(UUID tenantId, UUID vendorId, String invoiceNumber);

    boolean existsByTenantIdAndVendorIdAndInvoiceNumberIgnoreCase(
            UUID tenantId, UUID vendorId, String invoiceNumber);

    boolean existsByTenantIdAndVendorIdAndInvoiceNumberIgnoreCaseAndIdNot(
            UUID tenantId, UUID vendorId, String invoiceNumber, UUID id);

    @EntityGraph(attributePaths = {"vendor"})
    @Query(
            """
            SELECT i FROM Invoice i
            WHERE i.tenantId = :tenantId
              AND i.invoiceDate >= :fromDate
              AND i.invoiceDate <= :toDate
              AND i.taxAmount > 0
            ORDER BY i.invoiceDate DESC, i.invoiceNumber
            """)
    List<Invoice> findTaxableInvoicesForPeriod(
            @Param("tenantId") UUID tenantId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query(
            """
            SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i
            WHERE i.tenantId = :tenantId
              AND i.purchaseOrder.id = :poId
              AND i.status NOT IN ('CANCELLED', 'REJECTED')
              AND (:excludeInvoiceId IS NULL OR i.id <> :excludeInvoiceId)
            """)
    BigDecimal sumTotalAmountByPurchaseOrder(
            @Param("tenantId") UUID tenantId,
            @Param("poId") UUID poId,
            @Param("excludeInvoiceId") UUID excludeInvoiceId);

    @EntityGraph(attributePaths = {"vendor"})
    @Query(
            """
            SELECT i FROM Invoice i
            WHERE i.tenantId = :tenantId
              AND i.status IN ('POSTED', 'APPROVED', 'PARTIALLY_PAID')
            ORDER BY i.invoiceDate ASC
            """)
    List<Invoice> findOpenPayablesForAging(@Param("tenantId") UUID tenantId);

    @EntityGraph(attributePaths = {"vendor"})
    @Query(
            """
            SELECT i FROM Invoice i
            WHERE i.tenantId = :tenantId
              AND i.vendor.id = :vendorId
              AND i.invoiceDate <= :toDate
              AND i.status NOT IN ('DRAFT', 'PENDING_APPROVAL', 'REJECTED')
            ORDER BY i.invoiceDate ASC, i.invoiceNumber ASC
            """)
    List<Invoice> findVendorInvoicesThroughDate(
            @Param("tenantId") UUID tenantId,
            @Param("vendorId") UUID vendorId,
            @Param("toDate") LocalDate toDate);

    boolean existsByTenantIdAndVendor_IdAndStatusAndGlJournalIdIsNotNull(
            UUID tenantId, UUID vendorId, Invoice.InvoiceStatus status);
}
