package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.BudgetChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BudgetChangeRepository extends JpaRepository<BudgetChange, UUID> {

    List<BudgetChange> findByBudgetVersionIdOrderByCreatedAtDesc(UUID budgetVersionId);

    List<BudgetChange> findByBudgetLineIdOrderByCreatedAtDesc(UUID budgetLineId);

    List<BudgetChange> findByBudgetVersionIdAndStatus(UUID budgetVersionId, BudgetChange.ChangeStatus status);

    List<BudgetChange> findByBudgetLineIdAndStatus(UUID budgetLineId, BudgetChange.ChangeStatus status);

    @Query("SELECT bc FROM BudgetChange bc WHERE bc.tenantId = :tenantId AND bc.status = :status")
    List<BudgetChange> findByTenantIdAndStatus(@Param("tenantId") String tenantId, 
                                               @Param("status") BudgetChange.ChangeStatus status);

    @Query("SELECT bc FROM BudgetChange bc WHERE bc.tenantId = :tenantId AND bc.createdAt >= :since")
    List<BudgetChange> findByTenantIdSince(@Param("tenantId") String tenantId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(bc) FROM BudgetChange bc WHERE bc.tenantId = :tenantId AND bc.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") BudgetChange.ChangeStatus status);

    @Query("SELECT bc FROM BudgetChange bc WHERE bc.tenantId = :tenantId AND bc.approvedBy = :approvedBy")
    List<BudgetChange> findByTenantIdAndApprovedBy(@Param("tenantId") String tenantId, @Param("approvedBy") String approvedBy);
}
