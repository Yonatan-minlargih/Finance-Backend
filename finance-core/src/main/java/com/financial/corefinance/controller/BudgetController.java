package com.financial.corefinance.controller;

import com.financial.corefinance.dto.request.BudgetRequest;
import com.financial.corefinance.dto.response.BudgetResponse;
import com.financial.corefinance.domain.entity.Budget;
import com.financial.corefinance.repository.BudgetRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Budget Management", description = "APIs for managing budgets and budget monitoring")
public class BudgetController {

    private final BudgetRepository budgetRepository;

    @PostMapping
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER')")
    @Operation(summary = "Create a new budget", description = "Creates a new budget for a fiscal year")
    public ResponseEntity<BudgetResponse> createBudget(@Valid @RequestBody BudgetRequest request) {
        log.info("Creating budget: {}", request.getBudgetName());
        Budget budget = toBudgetEntity(request);
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        if (budgetRepository.existsByTenantIdAndFiscalYearIdAndBudgetName(
                tenantId, budget.getFiscalYearId(), budget.getBudgetName())) {
            throw new IllegalArgumentException("Budget with name " + budget.getBudgetName() +
                    " already exists for this fiscal year");
        }

        Budget savedBudget = budgetRepository.save(budget);
        return ResponseEntity.status(HttpStatus.CREATED).body(toBudgetResponse(savedBudget));
    }

    @GetMapping("/{budgetId}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get budget by ID", description = "Retrieves a budget by its ID")
    public ResponseEntity<BudgetResponse> getBudget(
            @Parameter(description = "Budget ID") @PathVariable UUID budgetId) {
        log.info("Retrieving budget with ID: {}", budgetId);

        Optional<Budget> budget = budgetRepository.findById(budgetId);
        return budget.map(this::toBudgetResponse).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get all budgets", description = "Retrieves a paginated list of budgets")
    public ResponseEntity<Page<BudgetResponse>> getAllBudgets(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "budgetName") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        Page<BudgetResponse> budgets = budgetRepository.findByTenantId(tenantId, pageable).map(this::toBudgetResponse);
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/fiscal-year/{fiscalYearId}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get budgets by fiscal year", description = "Retrieves budgets for a specific fiscal year")
    public ResponseEntity<List<BudgetResponse>> getBudgetsByFiscalYear(
            @Parameter(description = "Fiscal year ID") @PathVariable UUID fiscalYearId) {

        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<BudgetResponse> budgets = budgetRepository.findByTenantIdAndFiscalYearId(tenantId, fiscalYearId).stream()
                .map(this::toBudgetResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get budgets by department", description = "Retrieves budgets for a specific department")
    public ResponseEntity<List<BudgetResponse>> getBudgetsByDepartment(
            @Parameter(description = "Department ID") @PathVariable UUID departmentId) {

        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<BudgetResponse> budgets = budgetRepository.findByTenantIdAndDepartmentId(tenantId, departmentId).stream()
                .map(this::toBudgetResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get budgets by status", description = "Retrieves budgets filtered by status")
    public ResponseEntity<List<BudgetResponse>> getBudgetsByStatus(
            @Parameter(description = "Budget status") @PathVariable Budget.BudgetStatus status) {

        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<BudgetResponse> budgets = budgetRepository.findByTenantIdAndStatus(tenantId, status).stream()
                .map(this::toBudgetResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(budgets);
    }
    @GetMapping("/type/{budgetType}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get budgets by type", description = "Retrieves budgets filtered by type")
    public ResponseEntity<List<BudgetResponse>> getBudgetsByType(
            @Parameter(description = "Budget type") @PathVariable Budget.BudgetType budgetType) {

        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<BudgetResponse> budgets = budgetRepository.findByTenantIdAndBudgetType(tenantId, budgetType).stream()
                .map(this::toBudgetResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Search budgets", description = "Searches budgets by name or description")
    public ResponseEntity<Page<BudgetResponse>> searchBudgets(
            @Parameter(description = "Search term") @RequestParam String search,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "budgetName"));

        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        Page<BudgetResponse> budgets = budgetRepository.searchBudgets(tenantId, search, pageable).map(this::toBudgetResponse);
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/fiscal-year/{fiscalYearId}/current")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get current budget", description = "Retrieves the current budget for a fiscal year")
    public ResponseEntity<BudgetResponse> getCurrentBudget(
            @Parameter(description = "Fiscal year ID") @PathVariable UUID fiscalYearId) {

        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        Optional<Budget> budget = budgetRepository.findCurrentBudget(tenantId, fiscalYearId);

        return budget.map(this::toBudgetResponse).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{budgetId}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER')")
    @Operation(summary = "Update budget", description = "Updates an existing budget")
    public ResponseEntity<BudgetResponse> updateBudget(
            @Parameter(description = "Budget ID") @PathVariable UUID budgetId,
            @Valid @RequestBody BudgetRequest request) {
        log.info("Updating budget with ID: {}", budgetId);

        if (!budgetRepository.existsById(budgetId)) {
            return ResponseEntity.notFound().build();
        }
        Budget budget = toBudgetEntity(request);
        budget.setId(budgetId);
        Budget updatedBudget = budgetRepository.save(budget);
        return ResponseEntity.ok(toBudgetResponse(updatedBudget));
    }
    @PostMapping("/{budgetId}/approve")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Approve budget", description = "Approves a budget and makes it active")
    public ResponseEntity<BudgetResponse> approveBudget(
            @Parameter(description = "Budget ID") @PathVariable UUID budgetId) {
        log.info("Approving budget with ID: {}", budgetId);

        Optional<Budget> budgetOpt = budgetRepository.findById(budgetId);
        if (budgetOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Budget budget = budgetOpt.get();
        budget.setStatus(Budget.BudgetStatus.APPROVED);
        budget.setApprovedAt(java.time.LocalDate.now());
        // budget.setApprovedBy(getCurrentUser()); // Would need to implement

        Budget updatedBudget = budgetRepository.save(budget);
        return ResponseEntity.ok(toBudgetResponse(updatedBudget));
    }

    @PostMapping("/{budgetId}/lock")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Lock budget", description = "Locks a budget to prevent further changes")
    public ResponseEntity<BudgetResponse> lockBudget(
            @Parameter(description = "Budget ID") @PathVariable UUID budgetId) {
        log.info("Locking budget with ID: {}", budgetId);

        Optional<Budget> budgetOpt = budgetRepository.findById(budgetId);
        if (budgetOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Budget budget = budgetOpt.get();
        budget.setLocked(true);
        budget.setLockedAt(java.time.LocalDate.now());
        // budget.setLockedBy(getCurrentUser()); // Would need to implement

        Budget updatedBudget = budgetRepository.save(budget);
        return ResponseEntity.ok(toBudgetResponse(updatedBudget));
    }

    @DeleteMapping("/{budgetId}")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Delete budget", description = "Deletes a budget (if not used in transactions)")
    public ResponseEntity<Void> deleteBudget(
            @Parameter(description = "Budget ID") @PathVariable UUID budgetId) {
        log.info("Deleting budget with ID: {}", budgetId);

        if (!budgetRepository.existsById(budgetId)) {
            return ResponseEntity.notFound().build();
        }

        // Additional validation would be needed to ensure budget is not locked or used
        budgetRepository.deleteById(budgetId);

        return ResponseEntity.noContent().build();
    }

    private Budget toBudgetEntity(BudgetRequest request) {
        Budget budget = new Budget();
        budget.setTenantId(request.getTenantId());
        budget.setFiscalYearId(request.getFiscalYearId());
        budget.setBudgetName(request.getBudgetName());
        budget.setDescription(request.getDescription());
        budget.setDepartmentId(request.getDepartmentId());
        budget.setBudgetType(request.getBudgetType());
        budget.setStatus(request.getStatus());
        budget.setCurrencyCode(request.getCurrencyCode());
        budget.setTotalBudgetAmount(request.getTotalBudgetAmount());
        budget.setApprovalRequired(request.getApprovalRequired());
        budget.setEffectiveFrom(request.getEffectiveFrom());
        budget.setEffectiveTo(request.getEffectiveTo());
        return budget;
    }
    private BudgetResponse toBudgetResponse(Budget budget) {
        BudgetResponse response = new BudgetResponse();
        response.setId(budget.getId());
        response.setTenantId(budget.getTenantId());
        response.setCreatedAt(budget.getCreatedAt());
        response.setUpdatedAt(budget.getUpdatedAt());
        response.setCreatedBy(budget.getCreatedBy());
        response.setUpdatedBy(budget.getUpdatedBy());
        response.setVersion(budget.getVersion());
        response.setFiscalYearId(budget.getFiscalYearId());
        response.setBudgetName(budget.getBudgetName());
        response.setDescription(budget.getDescription());
        response.setDepartmentId(budget.getDepartmentId());
        response.setBudgetType(budget.getBudgetType());
        response.setStatus(budget.getStatus());
        response.setCurrencyCode(budget.getCurrencyCode());
        response.setTotalBudgetAmount(budget.getTotalBudgetAmount());
        response.setTotalAllocatedAmount(budget.getTotalAllocatedAmount());
        response.setTotalActualAmount(budget.getTotalActualAmount());
        response.setTotalVariance(budget.getTotalVariance());
        response.setApprovalRequired(budget.getApprovalRequired());
        response.setApprovedAt(budget.getApprovedAt());
        response.setApprovedBy(budget.getApprovedBy());
        response.setLocked(budget.getLocked());
        response.setLockedAt(budget.getLockedAt());
        response.setLockedBy(budget.getLockedBy());
        response.setEffectiveFrom(budget.getEffectiveFrom());
        response.setEffectiveTo(budget.getEffectiveTo());
        return response;
    }
}