package com.finance.transactional.repository;

import com.finance.transactional.model.ap.PaymentAllocation;
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
public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, UUID>, JpaSpecificationExecutor<PaymentAllocation>  {
    List<PaymentAllocation> findByTenantId(UUID tenantId);

    Optional<PaymentAllocation> findByTenantIdAndId(UUID tenantId, UUID id);

    boolean existsByTenantIdAndInvoiceId(UUID tenantId, UUID invoiceId);

    @Query(
            """
            SELECT COALESCE(SUM(pa.allocatedAmount), 0) FROM PaymentAllocation pa
            WHERE pa.tenantId = :tenantId AND pa.invoice.id = :invoiceId
            """)
    BigDecimal sumAllocatedToInvoice(@Param("tenantId") UUID tenantId, @Param("invoiceId") UUID invoiceId);
}
