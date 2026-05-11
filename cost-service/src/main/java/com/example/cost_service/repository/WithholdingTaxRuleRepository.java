package com.example.cost_service.repository;

import com.example.cost_service.enums.ApplicableTo;
import com.example.cost_service.enums.TaxType;
import com.example.cost_service.model.WithholdingTaxRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WithholdingTaxRuleRepository extends JpaRepository<WithholdingTaxRule, UUID> {

    List<WithholdingTaxRule> findByTenantId(UUID tenantId);

    Optional<WithholdingTaxRule> findByTenantIdAndId(UUID tenantId, UUID id);

    List<WithholdingTaxRule> findByTenantIdAndTaxType(UUID tenantId, TaxType taxType);

    List<WithholdingTaxRule> findByTenantIdAndApplicableTo(UUID tenantId, ApplicableTo applicableTo);
}
