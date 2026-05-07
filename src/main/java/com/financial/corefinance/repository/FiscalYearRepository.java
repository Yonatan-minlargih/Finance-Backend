package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.FiscalYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FiscalYearRepository extends JpaRepository<FiscalYear, UUID> {

    Optional<FiscalYear> findByTenantIdAndYearNumber(String tenantId, Integer yearNumber);

    Optional<FiscalYear> findByTenantIdAndIsCurrentTrue(String tenantId);

    List<FiscalYear> findByTenantIdOrderByYearNumberDesc(String tenantId);

    List<FiscalYear> findByTenantIdAndCalendarDefinitionId(String tenantId, UUID calendarDefinitionId);

    List<FiscalYear> findByTenantIdAndIsClosedFalse(String tenantId);

    @Query("SELECT fy FROM FiscalYear fy WHERE fy.tenantId = :tenantId AND :date BETWEEN fy.startDate AND fy.endDate")
    Optional<FiscalYear> findFiscalYearForDate(@Param("tenantId") String tenantId, @Param("date") LocalDate date);

    @Query("SELECT fy FROM FiscalYear fy WHERE fy.tenantId = :tenantId AND fy.isCurrent = true")
    Optional<FiscalYear> findCurrentFiscalYear(@Param("tenantId") String tenantId);

    @Query("SELECT fy FROM FiscalYear fy WHERE fy.tenantId = :tenantId AND fy.isClosed = false ORDER BY fy.yearNumber")
    List<FiscalYear> findOpenFiscalYears(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(fy) FROM FiscalYear fy WHERE fy.tenantId = :tenantId AND fy.isCurrent = true")
    long countCurrentFiscalYears(@Param("tenantId") String tenantId);

    boolean existsByTenantIdAndYearNumber(String tenantId, Integer yearNumber);
}
