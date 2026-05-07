package com.finance.transactional.repository;

import com.finance.transactional.model.asset.FixedAsset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FixedAssetRepository extends JpaRepository<FixedAsset, UUID>, JpaSpecificationExecutor<FixedAsset>  {
    List<FixedAsset> findByTenantId(UUID tenantId);

    Optional<FixedAsset> findByTenantIdAndId(UUID tenantId, UUID id);
}
