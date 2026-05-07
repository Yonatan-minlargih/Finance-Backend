package com.example.payroll_service.repository;

import com.example.payroll_service.enums.EarningDeductionCategory;
import com.example.payroll_service.model.EarningDeductionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EarningDeductionTypeRepository extends JpaRepository<EarningDeductionType, UUID> {

    List<EarningDeductionType> findByTenantId(UUID tenantId);

    Optional<EarningDeductionType> findByTenantIdAndId(UUID tenantId, UUID id);

    List<EarningDeductionType> findByTenantIdAndCategory(UUID tenantId, EarningDeductionCategory category);

    boolean existsByTenantIdAndTypeName(UUID tenantId, String typeName);

    boolean existsByTenantIdAndTypeNameAndIdNot(UUID tenantId, String typeName, UUID id);

    @Query("select e from EarningDeductionType e where " +
           "e.tenantId = :tenantId AND " +
           "(lower(e.typeName) like lower(concat('%', :keyword, '%')))")
    List<EarningDeductionType> searchByTypeName(@Param("tenantId") UUID tenantId, @Param("keyword") String keyword);
}
