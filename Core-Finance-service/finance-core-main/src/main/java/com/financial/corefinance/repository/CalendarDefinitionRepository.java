package com.financial.corefinance.repository;

import com.financial.corefinance.domain.entity.CalendarDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalendarDefinitionRepository extends JpaRepository<CalendarDefinition, UUID> {
    List<CalendarDefinition> findByTenantId(String tenantId);
    Optional<CalendarDefinition> findByTenantIdAndIsDefaultTrue(String tenantId);
    boolean existsByTenantIdAndCalendarName(String tenantId, String calendarName);
}
