package com.finance.transactional.repository;

import com.finance.transactional.model.banking.BankAccount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, UUID>, JpaSpecificationExecutor<BankAccount>  {
    List<BankAccount> findByTenantId(UUID tenantId);

    Optional<BankAccount> findByTenantIdAndId(UUID tenantId, UUID id);
}
