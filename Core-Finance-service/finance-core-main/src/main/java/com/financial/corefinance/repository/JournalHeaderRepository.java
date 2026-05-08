package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.JournalHeader;
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
public interface JournalHeaderRepository extends JpaRepository<JournalHeader, UUID> {

    Optional<JournalHeader> findByTenantIdAndJournalNumber(String tenantId, String journalNumber);

    List<JournalHeader> findByTenantIdAndJournalDateBetween(String tenantId, LocalDate startDate, LocalDate endDate);

    List<JournalHeader> findByTenantIdAndAccountingPeriodId(String tenantId, UUID accountingPeriodId);

    List<JournalHeader> findByTenantIdAndStatus(String tenantId, JournalHeader.JournalStatus status);

    List<JournalHeader> findByTenantIdAndJournalType(String tenantId, JournalHeader.JournalType journalType);

    Page<JournalHeader> findByTenantId(String tenantId, Pageable pageable);

    @Query("SELECT j FROM JournalHeader j WHERE j.tenantId = :tenantId AND " +
            "(j.journalNumber LIKE %:search% OR j.description LIKE %:search% OR j.referenceNumber LIKE %:search%)")
    Page<JournalHeader> searchJournals(@Param("tenantId") String tenantId, @Param("search") String search, Pageable pageable);

    @Query("SELECT j FROM JournalHeader j WHERE j.tenantId = :tenantId AND j.status = :status AND j.journalDate <= :date")
    List<JournalHeader> findJournalsForPosting(@Param("tenantId") String tenantId,
                                               @Param("status") JournalHeader.JournalStatus status,
                                               @Param("date") LocalDate date);

    @Query("SELECT j FROM JournalHeader j WHERE j.tenantId = :tenantId AND j.isReversed = false AND j.originalJournalId IS NULL")
    List<JournalHeader> findJournalsAvailableForReversal(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(j) FROM JournalHeader j WHERE j.tenantId = :tenantId AND j.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") JournalHeader.JournalStatus status);

    boolean existsByTenantIdAndJournalNumber(String tenantId, String journalNumber);

    @Query("SELECT j FROM JournalHeader j JOIN AccountingPeriod p ON p.id = j.accountingPeriodId " +
            "WHERE j.tenantId = :tenantId AND j.journalType = :journalType AND p.fiscalYearId = :fiscalYearId")
    List<JournalHeader> findByTenantIdAndJournalTypeAndFiscalYearId(
            @Param("tenantId") String tenantId,
            @Param("journalType") JournalHeader.JournalType journalType,
            @Param("fiscalYearId") UUID fiscalYearId);
}