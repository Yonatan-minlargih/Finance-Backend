package com.finance.transactional.repository;

import com.finance.transactional.model.ap.Payment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment>  {
    List<Payment> findByTenantId(UUID tenantId);

    Optional<Payment> findByTenantIdAndId(UUID tenantId, UUID id);
}
