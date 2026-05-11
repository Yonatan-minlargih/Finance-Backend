package com.example.cost_service.repository;

import com.example.cost_service.model.CostRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CostRecordRepository extends JpaRepository<CostRecord, UUID> {

    List<CostRecord> findByTenantId(UUID tenantId);

    Optional<CostRecord> findByTenantIdAndId(UUID tenantId, UUID id);

    List<CostRecord> findByTenantIdAndProductId(UUID tenantId, UUID productId);

    List<CostRecord> findByTenantIdAndCostCenterId(UUID tenantId, UUID costCenterId);

    List<CostRecord> findByTenantIdAndPeriodId(UUID tenantId, UUID periodId);
}
