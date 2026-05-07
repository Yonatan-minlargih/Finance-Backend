package com.financial.corefinance.controller;

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
    public ResponseEntity<Budget> createBudget(@Valid @RequestBody Budget budget) {
        log.info("Creating budget: {}", budget.getBudgetName());
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        if (budgetRepository.existsByTenantIdAndFiscalYearIdAndBudgetName(
                tenantId, budget.getFiscalYearId(), budget.getBudgetName())) {
            throw new IllegalArgumentException("Budget with name " + budget.getBudgetName() + 
                " already exists for this fiscal year");
        }
        
        Budget savedBudget = budgetRepository.save(budget);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBudget);
    }

    @GetMapping("/{budgetId}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get budget by ID", description = "Retrieves a budget by its ID")
    public ResponseEntity<Budget> getBudget(
            @Parameter(description = "Budget ID") @PathVariable UUID budgetId) {
        log.info("Retrieving budget with ID: {}", budgetId);
        
        Optional<Budget> budget = budgetRepository.findById(budgetId);
        return budget.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get all budgets", description = "Retrieves a paginated list of budgets")
    public ResponseEntity<Page<Budget>> getAllBudgets(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "budgetName") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        Page<Budget> budgets = budgetRepository.findByTenantId(tenantId, pageable);
        
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/fiscal-year/{fiscalYearId}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get budgets by fiscal year", description = "Retrieves budgets for a specific fiscal year")
    public ResponseEntity<List<Budget>> getBudgetsByFiscalYear(
            @Parameter(description = "Fiscal year ID") @PathVariable UUID fiscalYearId) {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<Budget> budgets = budgetRepository.findByTenantIdAndFiscalYearId(tenantId, fiscalYearId);
        
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get budgets by department", description = "Retrieves budgets for a specific department")
    public ResponseEntity<List<Budget>> getBudgetsByDepartment(
            @Parameter(description = "Department ID") @PathVariable UUID departmentId) {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<Budget> budgets = budgetRepository.findByTenantIdAndDepartmentId(tenantId, departmentId);
        
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get budgets by status", description = "Retrieves budgets filtered by status")
    public ResponseEntity<List<Budget>> getBudgetsByStatus(
            @Parameter(description = "Budget status") @PathVariable Budget.BudgetStatus status) {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<Budget> budgets = budgetRepository.findByTenantIdAndStatus(tenantId, status);
        
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/type/{budgetType}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get budgets by type", description = "Retrieves budgets filtered by type")
    public ResponseEntity<List<Budget>> getBudgetsByType(
            @Parameter(description = "Budget type") @PathVariable Budget.BudgetType budgetType) {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<Budget> budgets = budgetRepository.findByTenantIdAndBudgetType(tenantId, budgetType);
        
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Search budgets", description = "Searches budgets by name or description")
    public ResponseEntity<Page<Budget>> searchBudgets(
            @Parameter(description = "Search term") @RequestParam String search,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "budgetName"));
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        Page<Budget> budgets = budgetRepository.searchBudgets(tenantId, search, pageable);
        
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/fiscal-year/{fiscalYearId}/current")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get current budget", description = "Retrieves the current budget for a fiscal year")
    public ResponseEntity<Budget> getCurrentBudget(
            @Parameter(description = "Fiscal year ID") @PathVariable UUID fiscalYearId) {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        Optional<Budget> budget = budgetRepository.findCurrentBudget(tenantId, fiscalYearId);
        
        return budget.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{budgetId}")
    @PreAuthorize("hasRole('FINANCE_MANAGER') or hasRole('BUDGET_MANAGER')")
    @Operation(summary = "Update budget", description = "Updates an existing budget")
    public ResponseEntity<Budget> updateBudget(
            @Parameter(description = "Budget ID") @PathVariable UUID budgetId,
            @Valid @RequestBody Budget budget) {
        log.info("Updating budget with ID: {}", budgetId);
        
        if (!budgetRepository.existsById(budgetId)) {
            return ResponseEntity.notFound().build();
        }
        
        budget.setId(budgetId);
        Budget updatedBudget = budgetRepository.save(budget);
        
        return ResponseEntity.ok(updatedBudget);
    }

    @PostMapping("/{budgetId}/approve")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Approve budget", description = "Approves a budget and makes it active")
    public ResponseEntity<Budget> approveBudget(
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
        return ResponseEntity.ok(updatedBudget);
    }

    @PostMapping("/{budgetId}/lock")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Lock budget", description = "Locks a budget to prevent further changes")
    public ResponseEntity<Budget> lockBudget(
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
        return ResponseEntity.ok(updatedBudget);
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
}
