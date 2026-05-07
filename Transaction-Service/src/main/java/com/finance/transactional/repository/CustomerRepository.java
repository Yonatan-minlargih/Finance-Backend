package com.finance.transactional.repository;

import com.finance.transactional.model.ar.Customer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer>  {
    List<Customer> findByTenantId(UUID tenantId);

    Optional<Customer> findByTenantIdAndId(UUID tenantId, UUID id);
}
