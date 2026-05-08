package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByTenantIdAndAccountCode(String tenantId, String accountCode);

    List<Account> findByTenantIdAndParentAccountIdIsNull(String tenantId);

    List<Account> findByTenantIdAndParentAccountId(String tenantId, UUID parentAccountId);

    List<Account> findByTenantIdAndAccountType(String tenantId, Account.AccountType accountType);

    List<Account> findByTenantIdAndIfrsCategory(String tenantId, Account.IFRSCategory ifrsCategory);

    List<Account> findByTenantIdAndIsActiveTrue(String tenantId);

    @Query("SELECT a FROM Account a WHERE a.tenantId = :tenantId AND " +
           "(a.accountCode LIKE %:search% OR a.accountName LIKE %:search%)")
    List<Account> searchAccounts(@Param("tenantId") String tenantId, @Param("search") String search);

    @Query("SELECT a FROM Account a WHERE a.tenantId = :tenantId AND a.level = :level")
    List<Account> findByTenantIdAndLevel(@Param("tenantId") String tenantId, @Param("level") Integer level);

    @Query("SELECT a FROM Account a WHERE a.tenantId = :tenantId AND a.allowManualEntry = true")
    List<Account> findAccountsForManualEntry(@Param("tenantId") String tenantId);

    @Query("SELECT a FROM Account a WHERE a.tenantId = :tenantId AND a.reconciliationRequired = true")
    List<Account> findReconciliationRequiredAccounts(@Param("tenantId") String tenantId);

    boolean existsByTenantIdAndAccountCode(String tenantId, String accountCode);
}
