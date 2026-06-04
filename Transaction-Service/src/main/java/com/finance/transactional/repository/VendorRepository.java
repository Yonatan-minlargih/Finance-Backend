package com.finance.transactional.repository;

import com.finance.transactional.model.ap.Vendor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID>, JpaSpecificationExecutor<Vendor>  {
    List<Vendor> findByTenantId(UUID tenantId);

    Optional<Vendor> findByTenantIdAndId(UUID tenantId, UUID id);

    boolean existsByTenantIdAndVendorCodeIgnoreCase(UUID tenantId, String vendorCode);

    boolean existsByTenantIdAndVendorCodeIgnoreCaseAndIdNot(UUID tenantId, String vendorCode, UUID id);

    boolean existsByTenantIdAndTaxIdIgnoreCase(UUID tenantId, String taxId);

    boolean existsByTenantIdAndTaxIdIgnoreCaseAndIdNot(UUID tenantId, String taxId, UUID id);

    @Query("""
            SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END
            FROM Vendor v
            WHERE v.tenantId = :tenantId
              AND LOWER(v.vendorName) = LOWER(:vendorName)
              AND LOWER(v.taxId) = LOWER(:taxId)
            """)
    boolean existsByTenantIdAndVendorNameAndTaxId(
            @Param("tenantId") UUID tenantId,
            @Param("vendorName") String vendorName,
            @Param("taxId") String taxId);

    @Query("""
            SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END
            FROM Vendor v
            WHERE v.tenantId = :tenantId
              AND LOWER(v.vendorName) = LOWER(:vendorName)
              AND LOWER(v.taxId) = LOWER(:taxId)
              AND v.id <> :excludeId
            """)
    boolean existsByTenantIdAndVendorNameAndTaxIdAndIdNot(
            @Param("tenantId") UUID tenantId,
            @Param("vendorName") String vendorName,
            @Param("taxId") String taxId,
            @Param("excludeId") UUID excludeId);
}
