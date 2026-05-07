package com.finance.transactional.repository;

import com.finance.transactional.model.ap.InvoiceLine;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, UUID>, JpaSpecificationExecutor<InvoiceLine>  {
    List<InvoiceLine> findByTenantId(UUID tenantId);

    Optional<InvoiceLine> findByTenantIdAndId(UUID tenantId, UUID id);
}
