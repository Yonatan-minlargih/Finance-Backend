package com.finance.transactional.repository;

import com.finance.transactional.model.ap.VendorAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorAddressRepository extends JpaRepository<VendorAddress, UUID>, JpaSpecificationExecutor<VendorAddress>  {
    List<VendorAddress> findByTenantId(UUID tenantId);

    Optional<VendorAddress> findByTenantIdAndId(UUID tenantId, UUID id);
}
