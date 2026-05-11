package com.example.cost_service.repository;

import com.example.cost_service.model.ProfitabilityAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfitabilityAnalysisRepository extends JpaRepository<ProfitabilityAnalysis, UUID> {

    List<ProfitabilityAnalysis> findByTenantId(UUID tenantId);

    Optional<ProfitabilityAnalysis> findByTenantIdAndId(UUID tenantId, UUID id);

    List<ProfitabilityAnalysis> findByTenantIdAndProductId(UUID tenantId, UUID productId);

    List<ProfitabilityAnalysis> findByTenantIdAndCostCenterId(UUID tenantId, UUID costCenterId);

    List<ProfitabilityAnalysis> findByTenantIdAndProfitCenterId(UUID tenantId, UUID profitCenterId);

    List<ProfitabilityAnalysis> findByTenantIdAndPeriodId(UUID tenantId, UUID periodId);
}
