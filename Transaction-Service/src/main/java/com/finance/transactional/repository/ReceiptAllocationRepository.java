package com.finance.transactional.repository;

import com.finance.transactional.model.ar.ReceiptAllocation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptAllocationRepository extends JpaRepository<ReceiptAllocation, UUID>, JpaSpecificationExecutor<ReceiptAllocation>  {
    List<ReceiptAllocation> findByTenantId(UUID tenantId);

    Optional<ReceiptAllocation> findByTenantIdAndId(UUID tenantId, UUID id);
}
