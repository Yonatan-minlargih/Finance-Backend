package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.BudgetVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetVersionRepository extends JpaRepository<BudgetVersion, UUID> {

    boolean existsByBudgetIdAndVersionNumber(UUID budgetId, Integer versionNumber);

    List<BudgetVersion> findByBudgetIdOrderByVersionNumberDesc(UUID budgetId);

    Optional<BudgetVersion> findByBudgetIdAndIsCurrentTrue(UUID budgetId);

    List<BudgetVersion> findByBudgetIdAndStatus(UUID budgetId, BudgetVersion.BudgetVersionStatus status);

    @Query("SELECT bv FROM BudgetVersion bv WHERE bv.tenantId = :tenantId AND bv.isCurrent = true")
    List<BudgetVersion> findCurrentBudgetVersions(@Param("tenantId") String tenantId);

    @Query("SELECT bv FROM BudgetVersion bv WHERE bv.tenantId = :tenantId AND bv.status = :status")
    List<BudgetVersion> findByTenantIdAndStatus(@Param("tenantId") String tenantId, 
                                                @Param("status") BudgetVersion.BudgetVersionStatus status);

    @Query("SELECT COUNT(bv) FROM BudgetVersion bv WHERE bv.tenantId = :tenantId AND bv.budgetId = :budgetId")
    long countByTenantIdAndBudgetId(@Param("tenantId") String tenantId, @Param("budgetId") UUID budgetId);
}
