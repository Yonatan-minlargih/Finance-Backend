package com.example.cost_service.repository;

import com.example.cost_service.enums.ActiveStatus;
import com.example.cost_service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByTenantId(UUID tenantId);

    Optional<Product> findByTenantIdAndId(UUID tenantId, UUID id);

    boolean existsByTenantIdAndName(UUID tenantId, String name);

    boolean existsByTenantIdAndNameAndIdNot(UUID tenantId, String name, UUID id);

    List<Product> findByTenantIdAndIsActive(UUID tenantId, ActiveStatus status);
}
