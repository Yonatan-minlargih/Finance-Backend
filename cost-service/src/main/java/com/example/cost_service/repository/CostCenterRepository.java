package com.example.cost_service.repository;

import com.example.cost_service.enums.CostCenterType;
import com.example.cost_service.model.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CostCenterRepository extends JpaRepository<CostCenter, UUID> {

    List<CostCenter> findByTenantId(UUID tenantId);

    Optional<CostCenter> findByTenantIdAndId(UUID tenantId, UUID id);

    List<CostCenter> findByTenantIdAndProfitCenterId(UUID tenantId, UUID profitCenterId);

    List<CostCenter> findByTenantIdAndType(UUID tenantId, CostCenterType type);
}
