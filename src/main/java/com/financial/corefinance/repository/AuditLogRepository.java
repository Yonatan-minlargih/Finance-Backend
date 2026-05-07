package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByTenantIdAndEntityTypeAndEntityId(String tenantId, String entityType, UUID entityId);

    List<AuditLog> findByTenantIdAndUserId(String tenantId, String userId);

    List<AuditLog> findByTenantIdAndAction(String tenantId, AuditLog.AuditAction action);

    Page<AuditLog> findByTenantId(String tenantId, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.tenantId = :tenantId AND al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    List<AuditLog> findByTenantIdAndDateRange(@Param("tenantId") String tenantId, 
                                              @Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT al FROM AuditLog al WHERE al.tenantId = :tenantId AND " +
           "(al.entityType LIKE %:search% OR al.userName LIKE %:search% OR al.fieldName LIKE %:search%)")
    Page<AuditLog> searchAuditLogs(@Param("tenantId") String tenantId, @Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.tenantId = :tenantId AND al.action = :action AND al.createdAt >= :since")
    long countByTenantIdAndActionSince(@Param("tenantId") String tenantId, 
                                       @Param("action") AuditLog.AuditAction action, 
                                       @Param("since") LocalDateTime since);
}
