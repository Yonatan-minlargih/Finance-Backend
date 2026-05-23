package com.financial.corefinance.controller;

import com.financial.corefinance.domain.base.TenantContext;
import com.financial.corefinance.domain.entity.BudgetChange;
import com.financial.corefinance.dto.request.BudgetChangeRequest;
import com.financial.corefinance.dto.response.BudgetChangeResponse;
import com.financial.corefinance.repository.BudgetChangeRepository;
import com.financial.corefinance.repository.BudgetVersionRepository;
import com.financial.corefinance.repository.BudgetLineRepository;
import com.financial.corefinance.repository.AccountRepository;
import com.financial.corefinance.service.BudgetService;
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
@RequestMapping("/api/v1/budget-changes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Budget Change Management", description = "APIs for modifying and tracking budget changes")
public class BudgetChangeController {
    private final BudgetService budgetService;
    private final BudgetChangeRepository budgetChangeRepository;
    private final BudgetVersionRepository budgetVersionRepository;
    private final BudgetLineRepository budgetLineRepository;
    private final AccountRepository accountRepository;

    @PostMapping
    @Operation(summary = "Submit a budget change", description = "Creates a new budget change request")
    public ResponseEntity<BudgetChangeResponse> createBudgetChange(@Valid @RequestBody BudgetChangeRequest request) {
        log.info("Creating budget change for version ID: {}", request.getBudgetVersionId());
        
        BudgetChange budgetChange = toBudgetChangeEntity(request);
        BudgetChange savedChange = budgetService.createBudgetChange(budgetChange);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(toBudgetChangeResponse(savedChange));
    }

    @GetMapping
    @Operation(summary = "Get all budget changes", description = "Retrieves all budget changes for the current tenant")
    public ResponseEntity<List<BudgetChangeResponse>> getAllBudgetChanges() {
        log.info("Retrieving all budget changes for tenant");
        List<BudgetChangeResponse> changes = budgetChangeRepository.findAll().stream()
                .map(this::toBudgetChangeResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(changes);
    }

    @GetMapping("/version/{budgetVersionId}")
    @Operation(summary = "Get budget changes by version", description = "Retrieves all changes under a specific budget version")
    public ResponseEntity<List<BudgetChangeResponse>> getBudgetChanges(
            @Parameter(description = "Budget Version ID") @PathVariable UUID budgetVersionId) {
        log.info("Retrieving budget changes for version ID: {}", budgetVersionId);

        List<BudgetChangeResponse> changes = budgetService.getBudgetChanges(budgetVersionId).stream()
                .map(this::toBudgetChangeResponse)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(changes);
    }

    @PostMapping("/{budgetChangeId}/approve")
    @Operation(summary = "Approve budget change", description = "Approves a pending budget change and applies the amount")
    public ResponseEntity<BudgetChangeResponse> approveBudgetChange(
            @Parameter(description = "Budget Change ID") @PathVariable UUID budgetChangeId) {
        log.info("Approving budget change: {}", budgetChangeId);

        // Security context normally provides the approver
        String approvedBy = "System Admin"; 
        
        BudgetChange approvedChange = budgetService.approveBudgetChange(budgetChangeId, approvedBy);
        return ResponseEntity.ok(toBudgetChangeResponse(approvedChange));
    }

    @PostMapping("/{budgetChangeId}/reject")
    @Operation(summary = "Reject budget change", description = "Rejects a pending budget change")
    public ResponseEntity<BudgetChangeResponse> rejectBudgetChange(
            @Parameter(description = "Budget Change ID") @PathVariable UUID budgetChangeId) {
        log.info("Rejecting budget change: {}", budgetChangeId);
        String rejectedBy = TenantContext.getCurrentTenant() + " Admin";
        BudgetChange rejectedChange = budgetService.rejectBudgetChange(budgetChangeId, rejectedBy);
        return ResponseEntity.ok(toBudgetChangeResponse(rejectedChange));
    }

    private BudgetChange toBudgetChangeEntity(BudgetChangeRequest request) {
        BudgetChange change = new BudgetChange();
        change.setTenantId(request.getTenantId() != null ? request.getTenantId() : com.financial.corefinance.domain.base.TenantContext.getCurrentTenant());
        change.setBudgetVersionId(request.getBudgetVersionId());
        change.setBudgetLineId(request.getBudgetLineId());
        change.setChangeType(request.getChangeType());
        change.setOldAmount(request.getOldAmount());
        change.setNewAmount(request.getNewAmount());
        change.setReason(request.getReason());
        change.setAuthorityLevel(request.getAuthorityLevel());
        if(request.getStatus() != null) {
            change.setStatus(request.getStatus());
        }
        change.setEffectiveDate(request.getEffectiveDate());
        return change;
    }

    private BudgetChangeResponse toBudgetChangeResponse(BudgetChange change) {
        BudgetChangeResponse response = new BudgetChangeResponse();
        response.setId(change.getId());
        response.setTenantId(change.getTenantId());
        response.setBudgetVersionId(change.getBudgetVersionId());
        response.setBudgetLineId(change.getBudgetLineId());
        response.setChangeType(change.getChangeType());
        response.setOldAmount(change.getOldAmount());
        response.setNewAmount(change.getNewAmount());
        response.setChangeAmount(change.getChangeAmount());
        response.setChangePercentage(change.getChangePercentage());
        response.setReason(change.getReason());
        response.setAuthorityLevel(change.getAuthorityLevel());
        response.setApprovedBy(change.getApprovedBy());
        response.setApprovedAt(change.getApprovedAt());
        response.setStatus(change.getStatus());
        response.setEffectiveDate(change.getEffectiveDate());
        response.setCreatedAt(change.getCreatedAt());
        response.setUpdatedAt(change.getUpdatedAt());
        response.setCreatedBy(change.getCreatedBy());
        response.setUpdatedBy(change.getUpdatedBy());
        response.setVersion(change.getVersion());

        // Resolve names
        if (change.getBudgetVersionId() != null) {
            budgetVersionRepository.findById(change.getBudgetVersionId())
                .ifPresent(v -> response.setVersionName(v.getVersionName()));
        }
        
        if (change.getBudgetLineId() != null) {
            budgetLineRepository.findById(change.getBudgetLineId())
                .ifPresent(line -> {
                    if (line.getAccountId() != null) {
                        accountRepository.findById(line.getAccountId())
                            .ifPresent(a -> {
                                response.setAccountCode(a.getAccountCode());
                                response.setAccountName(a.getAccountName());
                            });
                    }
                });
        }

        return response;
    }
}
