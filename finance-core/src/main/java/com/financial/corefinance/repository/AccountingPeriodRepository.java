package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.AccountingPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountingPeriodRepository extends JpaRepository<AccountingPeriod, UUID> {

    List<AccountingPeriod> findByTenantIdAndFiscalYearIdOrderByPeriodNumber(String tenantId, UUID fiscalYearId);

    Optional<AccountingPeriod> findByTenantIdAndFiscalYearIdAndPeriodNumber(String tenantId, UUID fiscalYearId, Integer periodNumber);

    List<AccountingPeriod> findByTenantIdAndIsOpenTrue(String tenantId);

    List<AccountingPeriod> findByTenantIdAndIsClosedFalse(String tenantId);

    @Query("SELECT ap FROM AccountingPeriod ap WHERE ap.tenantId = :tenantId AND :date BETWEEN ap.startDate AND ap.endDate")
    Optional<AccountingPeriod> findPeriodForDate(@Param("tenantId") String tenantId, @Param("date") LocalDate date);

    @Query("SELECT ap FROM AccountingPeriod ap WHERE ap.tenantId = :tenantId AND ap.fiscalYearId = :fiscalYearId AND ap.isClosed = false ORDER BY ap.periodNumber")
    List<AccountingPeriod> findOpenPeriodsByFiscalYear(@Param("tenantId") String tenantId, @Param("fiscalYearId") UUID fiscalYearId);

    @Query("SELECT ap FROM AccountingPeriod ap WHERE ap.tenantId = :tenantId AND ap.fiscalYearId = :fiscalYearId AND ap.isAdjustmentPeriod = true")
    List<AccountingPeriod> findAdjustmentPeriods(@Param("tenantId") String tenantId, @Param("fiscalYearId") UUID fiscalYearId);

    @Query("SELECT COUNT(ap) FROM AccountingPeriod ap WHERE ap.tenantId = :tenantId AND ap.fiscalYearId = :fiscalYearId AND ap.isClosed = false")
    long countOpenPeriodsByFiscalYear(@Param("tenantId") String tenantId, @Param("fiscalYearId") UUID fiscalYearId);

    boolean existsByTenantIdAndFiscalYearIdAndPeriodNumber(String tenantId, UUID fiscalYearId, Integer periodNumber);
}
