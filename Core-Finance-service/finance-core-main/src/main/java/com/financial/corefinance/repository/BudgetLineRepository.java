package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.BudgetLine;
import com.financial.corefinance.domain.entity.BudgetLine.LineCategory;
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

    @Query("SELECT bl FROM BudgetLine bl JOIN Budget b ON bl.budgetId = b.id "
            + "WHERE bl.tenantId = :tenantId AND b.fiscalYearId = :fiscalYearId "
            + "AND bl.lineCategory = :lineCategory ORDER BY bl.periodNumber")
    List<BudgetLine> findForecastsByFiscalYear(
            @Param("tenantId") String tenantId,
            @Param("fiscalYearId") UUID fiscalYearId,
            @Param("lineCategory") LineCategory lineCategory);

    @Query("SELECT bl FROM BudgetLine bl WHERE bl.budgetId = :budgetId "
            + "AND (bl.lineCategory = :lineCategory OR bl.lineCategory IS NULL)")
    List<BudgetLine> findByBudgetIdAndLineCategory(
            @Param("budgetId") UUID budgetId, @Param("lineCategory") LineCategory lineCategory);

    @Query("SELECT bl FROM BudgetLine bl WHERE bl.budgetVersionId = :budgetVersionId "
            + "AND (bl.lineCategory = :lineCategory OR bl.lineCategory IS NULL)")
    List<BudgetLine> findByBudgetVersionIdAndLineCategory(
            @Param("budgetVersionId") UUID budgetVersionId, @Param("lineCategory") LineCategory lineCategory);

    /**
     * Budget lines on the current approved version only (used when posting journals to GL).
     */
    @Query("SELECT bl FROM BudgetLine bl JOIN Budget b ON bl.budgetId = b.id "
            + "JOIN BudgetVersion bv ON bl.budgetVersionId = bv.id "
            + "WHERE bl.tenantId = :tenantId AND b.fiscalYearId = :fiscalYearId AND bl.accountId = :accountId "
            + "AND bv.isCurrent = true "
            + "AND (bl.lineCategory = 'BUDGET' OR bl.lineCategory IS NULL)")
    List<BudgetLine> findCurrentBudgetLinesForFiscalYearAccount(
            @Param("tenantId") String tenantId,
            @Param("fiscalYearId") UUID fiscalYearId,
            @Param("accountId") UUID accountId);

    @Query("SELECT bl FROM BudgetLine bl JOIN Budget b ON bl.budgetId = b.id "
            + "WHERE bl.tenantId = :tenantId AND b.fiscalYearId = :fiscalYearId "
            + "AND (bl.lineCategory = 'BUDGET' OR bl.lineCategory IS NULL)")
    List<BudgetLine> findAllBudgetLinesForFiscalYear(
            @Param("tenantId") String tenantId, @Param("fiscalYearId") UUID fiscalYearId);
}
