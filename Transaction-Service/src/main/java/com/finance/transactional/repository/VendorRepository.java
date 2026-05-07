package com.finance.transactional.repository;

import com.finance.transactional.model.ap.Vendor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID>, JpaSpecificationExecutor<Vendor>  {
    List<Vendor> findByTenantId(UUID tenantId);

    Optional<Vendor> findByTenantIdAndId(UUID tenantId, UUID id);
}
