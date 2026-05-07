package com.finance.transactional.repository;

import com.finance.transactional.model.asset.AssetTransaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetTransactionRepository extends JpaRepository<AssetTransaction, UUID>, JpaSpecificationExecutor<AssetTransaction>  {
    List<AssetTransaction> findByTenantId(UUID tenantId);

    Optional<AssetTransaction> findByTenantIdAndId(UUID tenantId, UUID id);
}
