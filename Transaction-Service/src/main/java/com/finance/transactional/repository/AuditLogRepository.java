package com.finance.transactional.repository;

import com.finance.transactional.model.system.AuditLog;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog>  {
    List<AuditLog> findByTenantId(UUID tenantId);

    Optional<AuditLog> findByTenantIdAndId(UUID tenantId, UUID id);
}
