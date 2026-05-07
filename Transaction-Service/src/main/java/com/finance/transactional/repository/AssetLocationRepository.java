package com.finance.transactional.repository;

import com.finance.transactional.model.asset.AssetLocation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetLocationRepository extends JpaRepository<AssetLocation, UUID>, JpaSpecificationExecutor<AssetLocation>  {
    List<AssetLocation> findByTenantId(UUID tenantId);

    Optional<AssetLocation> findByTenantIdAndId(UUID tenantId, UUID id);
}
