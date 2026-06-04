package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.TenantAccountingSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantAccountingSettingsRepository extends JpaRepository<TenantAccountingSettings, UUID> {
    Optional<TenantAccountingSettings> findByTenantId(String tenantId);
}
