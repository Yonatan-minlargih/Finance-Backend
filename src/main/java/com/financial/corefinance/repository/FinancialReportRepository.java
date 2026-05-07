package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.FinancialReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinancialReportRepository extends JpaRepository<FinancialReport, UUID> {

    Optional<FinancialReport> findByTenantIdAndFiscalYearIdAndReportTypeAndReportName(
        String tenantId, UUID fiscalYearId, FinancialReport.ReportType reportType, String reportName);

    List<FinancialReport> findByTenantIdAndFiscalYearId(String tenantId, UUID fiscalYearId);

    List<FinancialReport> findByTenantIdAndReportType(String tenantId, FinancialReport.ReportType reportType);

    List<FinancialReport> findByTenantIdAndStatus(String tenantId, FinancialReport.ReportStatus status);

    Page<FinancialReport> findByTenantId(String tenantId, Pageable pageable);

    @Query("SELECT fr FROM FinancialReport fr WHERE fr.tenantId = :tenantId AND " +
           "(fr.reportName LIKE %:search% OR fr.description LIKE %:search%)")
    Page<FinancialReport> searchReports(@Param("tenantId") String tenantId, @Param("search") String search, Pageable pageable);

    @Query("SELECT fr FROM FinancialReport fr WHERE fr.tenantId = :tenantId AND fr.fiscalYearId = :fiscalYearId AND fr.reportType = :reportType ORDER BY fr.asOfDate DESC")
    List<FinancialReport> findLatestReportsByType(@Param("tenantId") String tenantId, 
                                                @Param("fiscalYearId") UUID fiscalYearId, 
                                                @Param("reportType") FinancialReport.ReportType reportType);

    @Query("SELECT fr FROM FinancialReport fr WHERE fr.tenantId = :tenantId AND fr.asOfDate = :asOfDate")
    List<FinancialReport> findReportsAsOfDate(@Param("tenantId") String tenantId, @Param("asOfDate") LocalDate asOfDate);

    @Query("SELECT COUNT(fr) FROM FinancialReport fr WHERE fr.tenantId = :tenantId AND fr.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") FinancialReport.ReportStatus status);

    boolean existsByTenantIdAndFiscalYearIdAndReportTypeAndReportName(
        String tenantId, UUID fiscalYearId, FinancialReport.ReportType reportType, String reportName);
}
