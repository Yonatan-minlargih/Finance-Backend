package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.NumberingSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface NumberingSeriesRepository extends JpaRepository<NumberingSeries, java.util.UUID> {

    Optional<NumberingSeries> findByTenantIdAndSeriesCode(String tenantId, String seriesCode);

    List<NumberingSeries> findByTenantIdAndIsActiveTrue(String tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ns FROM NumberingSeries ns WHERE ns.tenantId = :tenantId AND ns.seriesCode = :seriesCode")
    Optional<NumberingSeries> findByTenantIdAndSeriesCodeForUpdate(@Param("tenantId") String tenantId, 
                                                                  @Param("seriesCode") String seriesCode);

    @Query("SELECT ns FROM NumberingSeries ns WHERE ns.tenantId = :tenantId AND ns.isActive = true ORDER BY ns.seriesCode")
    List<NumberingSeries> findActiveNumberingSeries(@Param("tenantId") String tenantId);

    boolean existsByTenantIdAndSeriesCode(String tenantId, String seriesCode);
}
