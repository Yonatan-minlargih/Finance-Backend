package com.finance.transactional.repository;

import com.finance.transactional.model.banking.BankReconciliation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BankReconciliationRepository extends JpaRepository<BankReconciliation, UUID>, JpaSpecificationExecutor<BankReconciliation>  {
    List<BankReconciliation> findByTenantId(UUID tenantId);

    Optional<BankReconciliation> findByTenantIdAndId(UUID tenantId, UUID id);
}
