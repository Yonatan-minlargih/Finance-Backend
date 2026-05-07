package com.finance.transactional.repository;

import com.finance.transactional.model.ap.Invoice;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice>  {
    List<Invoice> findByTenantId(UUID tenantId);

    Optional<Invoice> findByTenantIdAndId(UUID tenantId, UUID id);
}
