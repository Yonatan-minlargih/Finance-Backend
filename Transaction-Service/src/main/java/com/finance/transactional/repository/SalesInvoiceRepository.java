package com.finance.transactional.repository;

import com.finance.transactional.model.ar.SalesInvoice;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, UUID>, JpaSpecificationExecutor<SalesInvoice>  {
    @EntityGraph(attributePaths = {"customer"})
    List<SalesInvoice> findByTenantId(UUID tenantId);

    @EntityGraph(attributePaths = {"customer"})
    Optional<SalesInvoice> findByTenantIdAndId(UUID tenantId, UUID id);

    @EntityGraph(attributePaths = {"customer"})
    @Query(
            """
            SELECT s FROM SalesInvoice s
            WHERE s.tenantId = :tenantId
              AND s.status IN ('ISSUED', 'PARTIALLY_PAID')
            ORDER BY s.invoiceDate ASC
            """)
    List<SalesInvoice> findOpenReceivablesForAging(@Param("tenantId") UUID tenantId);

    @EntityGraph(attributePaths = {"customer"})
    @Query(
            """
            SELECT s FROM SalesInvoice s
            WHERE s.tenantId = :tenantId
              AND s.customer.id = :customerId
              AND s.status IN ('ISSUED', 'PARTIALLY_PAID')
            ORDER BY s.invoiceDate ASC, s.invoiceNumber ASC
            """)
    List<SalesInvoice> findOpenReceivablesForCustomer(
            @Param("tenantId") UUID tenantId, @Param("customerId") UUID customerId);
}
