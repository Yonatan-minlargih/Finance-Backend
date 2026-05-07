package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.JournalLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    @Query("SELECT jl FROM JournalLine jl WHERE jl.tenantId = :tenantId AND jl.accountId = :accountId AND jl.journalHeader.status = 'POSTED'")
    List<JournalLine> findPostedLinesByAccount(@Param("tenantId") String tenantId, @Param("accountId") UUID accountId);

    @Query("SELECT jl FROM JournalLine jl WHERE jl.tenantId = :tenantId AND jl.reconciled = false AND jl.journalHeader.status = 'POSTED'")
    List<JournalLine> findUnreconciledPostedLines(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(jl) FROM JournalLine jl WHERE jl.tenantId = :tenantId AND jl.journalHeaderId = :journalHeaderId")
    long countByJournalHeaderId(@Param("tenantId") String tenantId, @Param("journalHeaderId") UUID journalHeaderId);
}
