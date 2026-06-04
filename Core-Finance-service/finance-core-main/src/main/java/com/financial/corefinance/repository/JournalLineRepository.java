package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.JournalLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface JournalLineRepository extends JpaRepository<JournalLine, UUID> {

    List<JournalLine> findByTenantIdAndJournalHeaderId(String tenantId, UUID journalHeaderId);

    List<JournalLine> findByTenantIdAndAccountId(String tenantId, UUID accountId);

    List<JournalLine> findByTenantIdAndJournalHeaderIdAndAccountId(String tenantId, UUID journalHeaderId, UUID accountId);

    @Query("SELECT jl FROM JournalLine jl WHERE jl.tenantId = :tenantId AND jl.journalHeaderId = :journalHeaderId ORDER BY jl.lineNumber")
    List<JournalLine> findByJournalHeaderIdOrderByLineNumber(@Param("tenantId") String tenantId, 
                                                           @Param("journalHeaderId") UUID journalHeaderId);

    @Query("SELECT SUM(jl.debitAmount) FROM JournalLine jl WHERE jl.tenantId = :tenantId AND jl.journalHeaderId = :journalHeaderId")
    BigDecimal sumDebitAmountByJournalHeader(@Param("tenantId") String tenantId, @Param("journalHeaderId") UUID journalHeaderId);

    @Query("SELECT SUM(jl.creditAmount) FROM JournalLine jl WHERE jl.tenantId = :tenantId AND jl.journalHeaderId = :journalHeaderId")
    BigDecimal sumCreditAmountByJournalHeader(@Param("tenantId") String tenantId, @Param("journalHeaderId") UUID journalHeaderId);

    @Query("SELECT jl FROM JournalLine jl " +
           "JOIN FETCH jl.journalHeader jh " +
           "LEFT JOIN FETCH jl.account a " +
           "WHERE jl.tenantId = :tenantId AND jl.accountId = :accountId " +
           "AND jh.status = com.financial.corefinance.domain.entity.JournalHeader.JournalStatus.POSTED " +
           "ORDER BY jh.journalDate ASC, jl.lineNumber ASC")
    List<JournalLine> findPostedLinesByAccount(@Param("tenantId") String tenantId, @Param("accountId") UUID accountId);

    @Query("SELECT jl FROM JournalLine jl WHERE jl.tenantId = :tenantId AND jl.reconciled = false AND jl.journalHeader.status = 'POSTED'")
    List<JournalLine> findUnreconciledPostedLines(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(jl) FROM JournalLine jl WHERE jl.tenantId = :tenantId AND jl.journalHeaderId = :journalHeaderId")
    long countByJournalHeaderId(@Param("tenantId") String tenantId, @Param("journalHeaderId") UUID journalHeaderId);

    /**
     * Calculate aggregate debit and credit totals for a given account.
     * Includes all POSTED journals (original + reversal) so reversed entries net to zero.
     * Returns Object[]{totalDebit, totalCredit} or null.
     */
    @Query("SELECT SUM(jl.debitAmount), SUM(jl.creditAmount) " +
           "FROM JournalLine jl JOIN jl.journalHeader jh " +
           "WHERE jl.tenantId = :tenantId " +
           "AND jl.accountId = :accountId " +
           "AND jh.status = com.financial.corefinance.domain.entity.JournalHeader.JournalStatus.POSTED")
    List<Object[]> calculateAccountTotals(@Param("tenantId") String tenantId, @Param("accountId") UUID accountId);

    /**
     * Calculate aggregate debit and credit totals for a given account within a specific fiscal year.
     */
    @Query("SELECT SUM(jl.debitAmount), SUM(jl.creditAmount) " +
           "FROM JournalLine jl JOIN jl.journalHeader jh JOIN jh.accountingPeriod ap " +
           "WHERE jl.tenantId = :tenantId " +
           "AND jl.accountId = :accountId " +
           "AND ap.fiscalYearId = :fiscalYearId " +
           "AND jh.status = com.financial.corefinance.domain.entity.JournalHeader.JournalStatus.POSTED")
    List<Object[]> calculateAccountTotalsForFiscalYear(@Param("tenantId") String tenantId,
                                                  @Param("accountId") UUID accountId,
                                                  @Param("fiscalYearId") UUID fiscalYearId);

    @Query("SELECT SUM(COALESCE(jl.debitAmount, 0)), SUM(COALESCE(jl.creditAmount, 0)) "
            + "FROM JournalLine jl JOIN jl.journalHeader jh "
            + "WHERE jl.tenantId = :tenantId AND jl.accountId = :accountId "
            + "AND jh.status = com.financial.corefinance.domain.entity.JournalHeader.JournalStatus.POSTED "
            + "AND (:asOfDate IS NULL OR jh.journalDate <= :asOfDate) "
            + "AND (:departmentId IS NULL OR jl.departmentId = :departmentId)")
    List<Object[]> calculateAccountTotalsAsOf(
            @Param("tenantId") String tenantId,
            @Param("accountId") UUID accountId,
            @Param("asOfDate") LocalDate asOfDate,
            @Param("departmentId") UUID departmentId);

    @Query("SELECT SUM(COALESCE(jl.debitAmount, 0)), SUM(COALESCE(jl.creditAmount, 0)) "
            + "FROM JournalLine jl JOIN jl.journalHeader jh JOIN jh.accountingPeriod ap "
            + "WHERE jl.tenantId = :tenantId AND jl.accountId = :accountId "
            + "AND ap.fiscalYearId = :fiscalYearId "
            + "AND jh.status = com.financial.corefinance.domain.entity.JournalHeader.JournalStatus.POSTED "
            + "AND (:asOfDate IS NULL OR jh.journalDate <= :asOfDate)")
    List<Object[]> calculateAccountTotalsForFiscalYearAsOf(
            @Param("tenantId") String tenantId,
            @Param("accountId") UUID accountId,
            @Param("fiscalYearId") UUID fiscalYearId,
            @Param("asOfDate") LocalDate asOfDate);

    /**
     * Posted activity for an account between journal dates (inclusive).
     * Preferred for budget actuals — aligns with fiscal year calendar even if period FK differs.
     */
    @Query("SELECT SUM(COALESCE(jl.debitAmount, 0)), SUM(COALESCE(jl.creditAmount, 0)) "
            + "FROM JournalLine jl JOIN jl.journalHeader jh "
            + "WHERE jl.tenantId = :tenantId AND jl.accountId = :accountId "
            + "AND jh.status = com.financial.corefinance.domain.entity.JournalHeader.JournalStatus.POSTED "
            + "AND jh.journalDate >= :periodStart AND jh.journalDate <= :periodEnd")
    List<Object[]> calculateAccountTotalsForJournalDateRange(
            @Param("tenantId") String tenantId,
            @Param("accountId") UUID accountId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);

    /**
     * Budget actuals for an account between journal dates (inclusive).
     * Excludes CLOSING and OPENING_BALANCE journal types so that year-end balance
     * transfers do not inflate or deflate operational spending figures.
     * Only MANUAL, SYSTEM, REVERSAL, ADJUSTMENT, and RECLASSIFICATION entries count.
     */
    @Query("SELECT SUM(COALESCE(jl.debitAmount, 0)), SUM(COALESCE(jl.creditAmount, 0)) "
            + "FROM JournalLine jl JOIN jl.journalHeader jh "
            + "WHERE jl.tenantId = :tenantId AND jl.accountId = :accountId "
            + "AND jh.status = com.financial.corefinance.domain.entity.JournalHeader.JournalStatus.POSTED "
            + "AND jh.journalType NOT IN ("
            + "  com.financial.corefinance.domain.entity.JournalHeader.JournalType.CLOSING,"
            + "  com.financial.corefinance.domain.entity.JournalHeader.JournalType.OPENING_BALANCE"
            + ") "
            + "AND jh.journalDate >= :periodStart AND jh.journalDate <= :periodEnd")
    List<Object[]> calculateBudgetActualsForDateRange(
            @Param("tenantId") String tenantId,
            @Param("accountId") UUID accountId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);
}
