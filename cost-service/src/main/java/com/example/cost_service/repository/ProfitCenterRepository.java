package com.example.cost_service.repository;

import com.example.cost_service.enums.ActiveStatus;
import com.example.cost_service.model.ProfitCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfitCenterRepository extends JpaRepository<ProfitCenter, UUID> {

    List<ProfitCenter> findByTenantId(UUID tenantId);

    Optional<ProfitCenter> findByTenantIdAndId(UUID tenantId, UUID id);

    boolean existsByTenantIdAndCode(UUID tenantId, String code);

    boolean existsByTenantIdAndCodeAndIdNot(UUID tenantId, String code, UUID id);

    List<ProfitCenter> findByTenantIdAndIsActive(UUID tenantId, ActiveStatus status);
}
