package com.finance.transactional.repository;

import com.finance.transactional.model.ap.Payment;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment>  {
    List<Payment> findByTenantId(UUID tenantId);

    Optional<Payment> findByTenantIdAndId(UUID tenantId, UUID id);

    List<Payment> findByTenantIdAndInvoice_Id(UUID tenantId, UUID invoiceId);

    @Query(
            """
            SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
            WHERE p.tenantId = :tenantId
              AND p.invoice.purchaseOrder.id = :poId
              AND (:excludePaymentId IS NULL OR p.id <> :excludePaymentId)
            """)
    BigDecimal sumAmountByPurchaseOrder(
            @Param("tenantId") UUID tenantId,
            @Param("poId") UUID poId,
            @Param("excludePaymentId") UUID excludePaymentId);

    @Query(
            """
            SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
            WHERE p.tenantId = :tenantId
              AND p.invoice.id = :invoiceId
              AND (:excludePaymentId IS NULL OR p.id <> :excludePaymentId)
            """)
    BigDecimal sumAmountByInvoice(
            @Param("tenantId") UUID tenantId,
            @Param("invoiceId") UUID invoiceId,
            @Param("excludePaymentId") UUID excludePaymentId);

    @Query(
            """
            SELECT p FROM Payment p
            WHERE p.tenantId = :tenantId
              AND p.vendor.id = :vendorId
              AND p.paymentDate >= :fromDate
              AND p.paymentDate <= :toDate
            ORDER BY p.paymentDate ASC, p.paymentNumber ASC
            """)
    List<Payment> findByVendorAndDateRange(
            @Param("tenantId") UUID tenantId,
            @Param("vendorId") UUID vendorId,
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate);
}
