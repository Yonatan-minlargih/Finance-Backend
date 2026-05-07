package com.finance.transactional.repository;

import com.finance.transactional.model.ar.Receipt;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, UUID>, JpaSpecificationExecutor<Receipt>  {
    List<Receipt> findByTenantId(UUID tenantId);

    Optional<Receipt> findByTenantIdAndId(UUID tenantId, UUID id);
}
