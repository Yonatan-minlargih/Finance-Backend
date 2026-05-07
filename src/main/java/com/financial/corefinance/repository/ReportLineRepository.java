package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.ReportLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReportLineRepository extends JpaRepository<ReportLine, UUID> {

    List<ReportLine> findByFinancialReportIdOrderByLineNumber(UUID financialReportId);

    List<ReportLine> findByFinancialReportIdAndAccountId(UUID financialReportId, UUID accountId);

    List<ReportLine> findByTenantIdAndAccountId(String tenantId, UUID accountId);

    @Query("SELECT rl FROM ReportLine rl WHERE rl.tenantId = :tenantId AND rl.financialReportId = :financialReportId ORDER BY rl.lineNumber")
    List<ReportLine> findByTenantIdAndFinancialReportIdOrderByLineNumber(@Param("tenantId") String tenantId, @Param("financialReportId") UUID financialReportId);

    @Query("SELECT SUM(rl.currentPeriodAmount) FROM ReportLine rl WHERE rl.tenantId = :tenantId AND rl.financialReportId = :financialReportId AND rl.lineType = 'ACCOUNT'")
    BigDecimal sumCurrentPeriodAmount(@Param("tenantId") String tenantId, @Param("financialReportId") UUID financialReportId);

    @Query("SELECT SUM(rl.priorPeriodAmount) FROM ReportLine rl WHERE rl.tenantId = :tenantId AND rl.financialReportId = :financialReportId AND rl.lineType = 'ACCOUNT'")
    BigDecimal sumPriorPeriodAmount(@Param("tenantId") String tenantId, @Param("financialReportId") UUID financialReportId);

    @Query("SELECT rl FROM ReportLine rl WHERE rl.tenantId = :tenantId AND rl.lineDescription LIKE %:search%")
    List<ReportLine> searchReportLines(@Param("tenantId") String tenantId, @Param("search") String search);

    @Query("SELECT COUNT(rl) FROM ReportLine rl WHERE rl.tenantId = :tenantId AND rl.financialReportId = :financialReportId")
    long countByTenantIdAndFinancialReportId(@Param("tenantId") String tenantId, @Param("financialReportId") UUID financialReportId);
}
