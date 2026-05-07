package com.finance.transactional.repository;

import com.finance.transactional.model.ar.SalesInvoice;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, UUID>, JpaSpecificationExecutor<SalesInvoice>  {
    List<SalesInvoice> findByTenantId(UUID tenantId);

    Optional<SalesInvoice> findByTenantIdAndId(UUID tenantId, UUID id);
}
