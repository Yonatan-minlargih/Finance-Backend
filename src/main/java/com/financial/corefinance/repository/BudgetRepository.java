package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    Optional<Budget> findByTenantIdAndFiscalYearIdAndBudgetName(
            String tenantId,
            UUID fiscalYearId,
            String budgetName
    );

    List<Budget> findByTenantIdAndFiscalYearId(String tenantId, UUID fiscalYearId);

    List<Budget> findByTenantIdAndDepartmentId(String tenantId, UUID departmentId);

    List<Budget> findByTenantIdAndStatus(String tenantId, Budget.BudgetStatus status);

    List<Budget> findByTenantIdAndBudgetType(String tenantId, Budget.BudgetType budgetType);

    Page<Budget> findByTenantId(String tenantId, Pageable pageable);

    @Query("""
        SELECT b 
        FROM Budget b 
        WHERE b.tenantId = :tenantId 
        AND (b.budgetName LIKE %:search% OR b.description LIKE %:search%)
    """)
    Page<Budget> searchBudgets(
            @Param("tenantId") String tenantId,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("""
        SELECT b 
        FROM Budget b 
        WHERE b.tenantId = :tenantId 
        AND b.status = :status 
        AND b.locked = false
    """)
    List<Budget> findActiveBudgets(
            @Param("tenantId") String tenantId,
            @Param("status") Budget.BudgetStatus status
    );

    // ✅ FIXED: removed isCurrent (does not exist)
    @Query("""
        SELECT b 
        FROM Budget b 
        WHERE b.tenantId = :tenantId 
        AND b.fiscalYearId = :fiscalYearId 
        AND b.status = com.financial.corefinance.domain.entity.Budget.BudgetStatus.ACTIVE
    """)
    Optional<Budget> findCurrentBudget(
            @Param("tenantId") String tenantId,
            @Param("fiscalYearId") UUID fiscalYearId
    );

    @Query("""
        SELECT COUNT(b) 
        FROM Budget b 
        WHERE b.tenantId = :tenantId 
        AND b.status = :status
    """)
    long countByTenantIdAndStatus(
            @Param("tenantId") String tenantId,
            @Param("status") Budget.BudgetStatus status
    );

    boolean existsByTenantIdAndFiscalYearIdAndBudgetName(
            String tenantId,
            UUID fiscalYearId,
            String budgetName
    );
}