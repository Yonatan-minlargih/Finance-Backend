package com.finance.transactional.repository;

import com.finance.transactional.model.ar.ReceiptAllocation;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptAllocationRepository extends JpaRepository<ReceiptAllocation, UUID>, JpaSpecificationExecutor<ReceiptAllocation>  {
    List<ReceiptAllocation> findByTenantId(UUID tenantId);

    Optional<ReceiptAllocation> findByTenantIdAndId(UUID tenantId, UUID id);

    @Query(
            """
            SELECT COALESCE(SUM(ra.allocatedAmount), 0) FROM ReceiptAllocation ra
            WHERE ra.tenantId = :tenantId AND ra.salesInvoice.id = :salesInvoiceId
              AND (:excludeReceiptId IS NULL OR ra.receipt.id <> :excludeReceiptId)
            """)
    BigDecimal sumAllocatedToSalesInvoice(
            @Param("tenantId") UUID tenantId,
            @Param("salesInvoiceId") UUID salesInvoiceId,
            @Param("excludeReceiptId") UUID excludeReceiptId);
}
