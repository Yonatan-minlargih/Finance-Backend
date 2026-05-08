package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.IntegrationIngressLog;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationIngressLogRepository extends JpaRepository<IntegrationIngressLog, UUID> {

    Optional<IntegrationIngressLog> findByTenantIdAndIdempotencyKey(String tenantId, String idempotencyKey);

    Optional<IntegrationIngressLog> findByTenantIdAndEventId(String tenantId, String eventId);
}
