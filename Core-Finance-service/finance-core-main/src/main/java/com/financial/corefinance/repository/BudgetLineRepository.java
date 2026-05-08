package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.BudgetLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface BudgetLineRepository extends JpaRepository<BudgetLine, UUID> {

    List<BudgetLine> findByBudgetId(UUID budgetId);

    List<BudgetLine> findByBudgetVersionId(UUID budgetVersionId);

    List<BudgetLine> findByBudgetIdAndAccountId(UUID budgetId, UUID accountId);

    List<BudgetLine> findByBudgetVersionIdAndAccountId(UUID budgetVersionId, UUID accountId);

    List<BudgetLine> findByTenantIdAndAccountId(String tenantId, UUID accountId);

    @Query("SELECT bl FROM BudgetLine bl WHERE bl.tenantId = :tenantId AND bl.accountId = :accountId AND bl.budget.budgetType = :budgetType")
    List<BudgetLine> findByTenantIdAndAccountIdAndBudgetType(@Param("tenantId") String tenantId, 
                                                             @Param("accountId") UUID accountId,
                                                             @Param("budgetType") com.financial.corefinance.domain.entity.Budget.BudgetType budgetType);

    @Query("SELECT SUM(bl.budgetAmount) FROM BudgetLine bl WHERE bl.tenantId = :tenantId AND bl.budgetId = :budgetId")
    BigDecimal sumBudgetAmountByBudgetId(@Param("tenantId") String tenantId, @Param("budgetId") UUID budgetId);

    @Query("SELECT SUM(bl.actualAmount) FROM BudgetLine bl WHERE bl.tenantId = :tenantId AND bl.budgetId = :budgetId")
    BigDecimal sumActualAmountByBudgetId(@Param("tenantId") String tenantId, @Param("budgetId") UUID budgetId);

    @Query("SELECT SUM(bl.budgetAmount) FROM BudgetLine bl WHERE bl.tenantId = :tenantId AND bl.budgetVersionId = :budgetVersionId")
    BigDecimal sumBudgetAmountByBudgetVersionId(@Param("tenantId") String tenantId, @Param("budgetVersionId") UUID budgetVersionId);

    @Query("SELECT SUM(bl.actualAmount) FROM BudgetLine bl WHERE bl.tenantId = :tenantId AND bl.budgetVersionId = :budgetVersionId")
    BigDecimal sumActualAmountByBudgetVersionId(@Param("tenantId") String tenantId, @Param("budgetVersionId") UUID budgetVersionId);

    @Query("SELECT bl FROM BudgetLine bl WHERE bl.tenantId = :tenantId AND bl.availableAmount < 0")
    List<BudgetLine> findOverBudgetLines(@Param("tenantId") String tenantId);

    @Query("SELECT bl FROM BudgetLine bl WHERE bl.tenantId = :tenantId AND bl.varianceAmount < 0")
    List<BudgetLine> findNegativeVarianceLines(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(bl) FROM BudgetLine bl WHERE bl.tenantId = :tenantId AND bl.budgetId = :budgetId")
    long countByTenantIdAndBudgetId(@Param("tenantId") String tenantId, @Param("budgetId") UUID budgetId);
}
