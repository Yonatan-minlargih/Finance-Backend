package com.example.payroll_service.repository;

import com.example.payroll_service.enums.PayrollStatus;
import com.example.payroll_service.model.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, UUID> {

    List<PayrollRun> findByTenantId(UUID tenantId);

    Optional<PayrollRun> findByTenantIdAndId(UUID tenantId, UUID id);

    List<PayrollRun> findByTenantIdAndStatus(UUID tenantId, PayrollStatus status);

    List<PayrollRun> findByTenantIdAndRunDateBetween(UUID tenantId, LocalDate startDate, LocalDate endDate);

    boolean existsByTenantIdAndPeriodId(UUID tenantId, UUID periodId);

    boolean existsByTenantIdAndPeriodIdAndIdNot(UUID tenantId, UUID periodId, UUID id);

    @Query("select e from PayrollRun e where " +
           "e.tenantId = :tenantId AND " +
           "e.periodId = :periodId")
    List<PayrollRun> findByTenantIdAndPeriodId(@Param("tenantId") UUID tenantId, @Param("periodId") UUID periodId);
}
