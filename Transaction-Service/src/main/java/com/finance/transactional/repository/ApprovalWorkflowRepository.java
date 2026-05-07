package com.finance.transactional.repository;

import com.finance.transactional.model.system.ApprovalWorkflow;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, UUID>, JpaSpecificationExecutor<ApprovalWorkflow>  {
    List<ApprovalWorkflow> findByTenantId(UUID tenantId);

    Optional<ApprovalWorkflow> findByTenantIdAndId(UUID tenantId, UUID id);
}
