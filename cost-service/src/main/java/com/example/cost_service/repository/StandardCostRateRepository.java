package com.example.cost_service.repository;

import com.example.cost_service.model.StandardCostRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StandardCostRateRepository extends JpaRepository<StandardCostRate, UUID> {

    List<StandardCostRate> findByTenantId(UUID tenantId);

    Optional<StandardCostRate> findByTenantIdAndId(UUID tenantId, UUID id);

    List<StandardCostRate> findByTenantIdAndProductId(UUID tenantId, UUID productId);

    List<StandardCostRate> findByTenantIdAndItemCode(UUID tenantId, String itemCode);
}
