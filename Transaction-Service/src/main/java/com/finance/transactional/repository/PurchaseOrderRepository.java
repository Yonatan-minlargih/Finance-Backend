package com.finance.transactional.repository;

import com.finance.transactional.model.ap.PurchaseOrder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID>, JpaSpecificationExecutor<PurchaseOrder>  {
    List<PurchaseOrder> findByTenantId(UUID tenantId);

    Optional<PurchaseOrder> findByTenantIdAndId(UUID tenantId, UUID id);
}
