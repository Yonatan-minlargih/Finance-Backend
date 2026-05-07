package com.finance.transactional.repository;

import com.finance.transactional.model.asset.DepreciationSchedule;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DepreciationScheduleRepository extends JpaRepository<DepreciationSchedule, UUID>, JpaSpecificationExecutor<DepreciationSchedule>  {
    List<DepreciationSchedule> findByTenantId(UUID tenantId);

    Optional<DepreciationSchedule> findByTenantIdAndId(UUID tenantId, UUID id);
}
