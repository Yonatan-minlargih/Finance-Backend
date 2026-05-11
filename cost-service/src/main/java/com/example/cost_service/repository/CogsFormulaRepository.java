package com.example.cost_service.repository;

import com.example.cost_service.model.CogsFormula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CogsFormulaRepository extends JpaRepository<CogsFormula, UUID> {

    List<CogsFormula> findByTenantId(UUID tenantId);

    Optional<CogsFormula> findByTenantIdAndId(UUID tenantId, UUID id);

    List<CogsFormula> findByTenantIdAndPeriodId(UUID tenantId, UUID periodId);
}
