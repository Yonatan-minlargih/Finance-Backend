package com.finance.transactional.repository;

import com.finance.transactional.model.banking.BankTransaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, UUID>, JpaSpecificationExecutor<BankTransaction>  {
    List<BankTransaction> findByTenantId(UUID tenantId);

    Optional<BankTransaction> findByTenantIdAndId(UUID tenantId, UUID id);
}
