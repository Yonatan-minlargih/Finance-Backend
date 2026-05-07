package com.finance.transactional.repository;

import com.finance.transactional.model.ap.PaymentAllocation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, UUID>, JpaSpecificationExecutor<PaymentAllocation>  {
    List<PaymentAllocation> findByTenantId(UUID tenantId);

    Optional<PaymentAllocation> findByTenantIdAndId(UUID tenantId, UUID id);
}
