package com.financial.corefinance.controller;

import com.financial.corefinance.domain.base.TenantContext;
import com.financial.corefinance.domain.entity.BudgetVersion;
import com.financial.corefinance.dto.request.BudgetVersionRequest;
import com.financial.corefinance.dto.response.BudgetVersionResponse;
import com.financial.corefinance.repository.BudgetVersionRepository;
import com.financial.corefinance.service.BudgetService;
import com.financial.corefinance.repository.BudgetRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/budget-versions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Budget Version Management", description = "APIs for managing budget versions")
public class BudgetVersionController {
    private final BudgetService budgetService;
    private final BudgetVersionRepository budgetVersionRepository;
    private final BudgetRepository budgetRepository;

    @PostMapping
    @Operation(summary = "Create a new budget version", description = "Creates a new budget version for an existing budget")
    public ResponseEntity<BudgetVersionResponse> createBudgetVersion(@Valid @RequestBody BudgetVersionRequest request) {
        log.info("Creating budget version for budget ID: {}", request.getBudgetId());
        BudgetVersion budgetVersion = toBudgetVersionEntity(request);
        
        BudgetVersion savedVersion = budgetService.createBudgetVersion(budgetVersion);
        return ResponseEntity.status(HttpStatus.CREATED).body(toBudgetVersionResponse(savedVersion));
    }

    @GetMapping
    @Operation(summary = "Get all budget versions", description = "Retrieves all budget versions for the current tenant")
    public ResponseEntity<List<BudgetVersionResponse>> getAllBudgetVersions() {
        log.info("Retrieving all budget versions for tenant");
        String tenantId = TenantContext.getCurrentTenant();
        List<BudgetVersionResponse> versions = budgetVersionRepository.findAll().stream()
                .filter(v -> tenantId.equals(v.getTenantId()))
                .map(this::toBudgetVersionResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/budget/{budgetId}")
    @Operation(summary = "Get budget versions by budget ID", description = "Retrieves all versions under a specific budget")
    public ResponseEntity<List<BudgetVersionResponse>> getBudgetVersions(
            @Parameter(description = "Budget ID") @PathVariable UUID budgetId) {
        log.info("Retrieving budget versions for budget ID: {}", budgetId);

        List<BudgetVersionResponse> versions = budgetService.getBudgetVersions(budgetId).stream()
                .map(this::toBudgetVersionResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(versions);
    }

    @PutMapping("/{budgetVersionId}")
    @Operation(summary = "Update budget version metadata", description = "Updates name, description, totals, and effective dates")
    public ResponseEntity<BudgetVersionResponse> updateBudgetVersion(
            @PathVariable UUID budgetVersionId,
            @Valid @RequestBody BudgetVersionRequest request) {
        BudgetVersion updates = toBudgetVersionEntity(request);
        BudgetVersion updated = budgetService.updateBudgetVersion(budgetVersionId, updates);
        return ResponseEntity.ok(toBudgetVersionResponse(updated));
    }

    @PostMapping("/{budgetVersionId}/set-current")
    @Operation(
            summary = "Set as current (working) version",
            description = "Marks the approved version used for budget vs actual monitoring and overspend checks. "
                    + "Only one current version per budget.")
    public ResponseEntity<BudgetVersionResponse> setCurrentBudgetVersion(
            @Parameter(description = "Budget Version ID") @PathVariable UUID budgetVersionId) {
        log.info("Setting budget version as current: {}", budgetVersionId);

        BudgetVersion updatedVersion = budgetService.setCurrentBudgetVersion(budgetVersionId);
        return ResponseEntity.ok(toBudgetVersionResponse(updatedVersion));
    }

    @PostMapping("/{budgetVersionId}/set-baseline")
    @Operation(
            summary = "Set as baseline (reference) version",
            description = "Marks the original approved plan kept for comparison in version-compare reports. "
                    + "Does not drive live actuals; only one baseline per budget.")
    public ResponseEntity<BudgetVersionResponse> setBaselineBudgetVersion(
            @PathVariable UUID budgetVersionId) {
        BudgetVersion updated = budgetService.setBaselineBudgetVersion(budgetVersionId);
        return ResponseEntity.ok(toBudgetVersionResponse(updated));
    }

    @PostMapping("/{budgetVersionId}/approve")
    @Operation(summary = "Approve budget version", description = "Approves a draft budget version")
    public ResponseEntity<BudgetVersionResponse> approveBudgetVersion(
            @Parameter(description = "Budget Version ID") @PathVariable UUID budgetVersionId) {
        log.info("Approving budget version: {}", budgetVersionId);
        String approvedBy = TenantContext.getCurrentTenant() + " Admin"; // Using a placeholder for auth context
        BudgetVersion updatedVersion = budgetService.approveBudgetVersion(budgetVersionId, approvedBy);
        return ResponseEntity.ok(toBudgetVersionResponse(updatedVersion));
    }

    @PostMapping("/{budgetVersionId}/unapprove")
    @Operation(summary = "Unapprove budget version", description = "Reverts approved budget version back to draft")
    public ResponseEntity<BudgetVersionResponse> unapproveBudgetVersion(
            @Parameter(description = "Budget Version ID") @PathVariable UUID budgetVersionId) {
        log.info("Unapproving budget version: {}", budgetVersionId);
        BudgetVersion updatedVersion = budgetService.unapproveBudgetVersion(budgetVersionId);
        return ResponseEntity.ok(toBudgetVersionResponse(updatedVersion));
    }

    @PostMapping("/{budgetVersionId}/archive")
    @Operation(summary = "Archive budget version", description = "Archives a budget version so it is no longer active")
    public ResponseEntity<BudgetVersionResponse> archiveBudgetVersion(
            @Parameter(description = "Budget Version ID") @PathVariable UUID budgetVersionId) {
        log.info("Archiving budget version: {}", budgetVersionId);
        BudgetVersion updatedVersion = budgetService.archiveBudgetVersion(budgetVersionId);
        return ResponseEntity.ok(toBudgetVersionResponse(updatedVersion));
    }

    private BudgetVersion toBudgetVersionEntity(BudgetVersionRequest request) {
        BudgetVersion version = new BudgetVersion();
        version.setTenantId(request.getTenantId() != null ? request.getTenantId() : com.financial.corefinance.domain.base.TenantContext.getCurrentTenant());
        version.setBudgetId(request.getBudgetId());
        version.setVersionNumber(request.getVersionNumber());
        version.setVersionName(request.getVersionName());
        version.setDescription(request.getDescription());
        if(request.getStatus() != null) {
            version.setStatus(request.getStatus());
        }
        version.setTotalBudgetAmount(request.getTotalBudgetAmount());
        version.setIsCurrent(request.getIsCurrent());
        version.setIsBaseline(request.getIsBaseline());
        version.setEffectiveFrom(request.getEffectiveFrom());
        version.setEffectiveTo(request.getEffectiveTo());
        return version;
    }

    private BudgetVersionResponse toBudgetVersionResponse(BudgetVersion version) {
        BudgetVersionResponse response = new BudgetVersionResponse();
        response.setId(version.getId());
        response.setBudgetId(version.getBudgetId());
        response.setTenantId(version.getTenantId());
        response.setVersionNumber(version.getVersionNumber());
        response.setVersionName(version.getVersionName());
        budgetRepository.findById(version.getBudgetId()).ifPresent(b -> response.setBudgetName(b.getBudgetName()));
        response.setDescription(version.getDescription());
        response.setStatus(version.getStatus());
        response.setTotalBudgetAmount(version.getTotalBudgetAmount());
        response.setTotalAllocatedAmount(version.getTotalAllocatedAmount());
        response.setTotalActualAmount(version.getTotalActualAmount());
        response.setTotalVariance(version.getTotalVariance());
        response.setIsCurrent(version.getIsCurrent());
        response.setIsBaseline(version.getIsBaseline());
        response.setApprovedAt(version.getApprovedAt());
        response.setApprovedBy(version.getApprovedBy());
        response.setEffectiveFrom(version.getEffectiveFrom());
        response.setEffectiveTo(version.getEffectiveTo());
        response.setCreatedAt(version.getCreatedAt());
        response.setUpdatedAt(version.getUpdatedAt());
        response.setCreatedBy(version.getCreatedBy());
        response.setUpdatedBy(version.getUpdatedBy());
        response.setVersion(version.getVersion());
        return response;
    }
}
