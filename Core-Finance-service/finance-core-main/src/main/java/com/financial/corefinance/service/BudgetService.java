package com.financial.corefinance.service;

import com.financial.corefinance.domain.entity.Budget;
import com.financial.corefinance.domain.entity.BudgetLine;
import com.financial.corefinance.domain.entity.BudgetVersion;
import com.financial.corefinance.domain.entity.BudgetChange;
import com.financial.corefinance.exception.BudgetValidationException;
import com.financial.corefinance.repository.*;
import com.financial.corefinance.event.FinanceEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetVersionRepository budgetVersionRepository;
    private final BudgetLineRepository budgetLineRepository;
    private final BudgetChangeRepository budgetChangeRepository;
    private final FiscalYearRepository fiscalYearRepository;
    private final FinanceEventService financeEventService;

    @Transactional
    public Budget createBudget(@Valid Budget budget) {
        log.info("Creating budget: {}", budget.getBudgetName());
        
        // Validate fiscal year exists and is not closed
        if (!fiscalYearRepository.existsById(budget.getFiscalYearId())) {
            throw new BudgetValidationException("Fiscal year not found: " + budget.getFiscalYearId());
        }
        
        // Validate budget name uniqueness within fiscal year
        String tenantId = budget.getTenantId();
        if (budgetRepository.existsByTenantIdAndFiscalYearIdAndBudgetName(
                tenantId, budget.getFiscalYearId(), budget.getBudgetName())) {
            throw new BudgetValidationException("Budget with name " + budget.getBudgetName() + 
                " already exists for this fiscal year");
        }
        
        // Set default values
        if (budget.getStatus() == null) {
            budget.setStatus(Budget.BudgetStatus.DRAFT);
        }
        if (budget.getBudgetType() == null) {
            budget.setBudgetType(Budget.BudgetType.OPERATING);
        }
        if (budget.getCurrencyCode() == null) {
            budget.setCurrencyCode("USD");
        }
        if (budget.getTotalAllocatedAmount() == null) {
            budget.setTotalAllocatedAmount(BigDecimal.ZERO);
        }
        if (budget.getTotalActualAmount() == null) {
            budget.setTotalActualAmount(BigDecimal.ZERO);
        }
        if (budget.getTotalVariance() == null) {
            budget.setTotalVariance(BigDecimal.ZERO);
        }
        if (budget.getApprovalRequired() == null) {
            budget.setApprovalRequired(true);
        }
        if (budget.getLocked() == null) {
            budget.setLocked(false);
        }
        Budget savedBudget = budgetRepository.save(budget);
        financeEventService.publishBudgetCreatedEvent(savedBudget.getId(), savedBudget.getTenantId(), "system");
        log.info("Budget created successfully: {}", savedBudget.getBudgetName());
        return savedBudget;
    }

    @Transactional
    public BudgetVersion createBudgetVersion(@Valid BudgetVersion budgetVersion) {
        log.info("Creating budget version: {} for budget: {}", 
                budgetVersion.getVersionNumber(), budgetVersion.getBudgetId());
        
        // Validate budget exists
        Optional<Budget> budgetOpt = budgetRepository.findById(budgetVersion.getBudgetId());
        if (budgetOpt.isEmpty()) {
            throw new BudgetValidationException("Budget not found: " + budgetVersion.getBudgetId());
        }
        
        Budget budget = budgetOpt.get();
        assertBudgetUnlocked(budget);
        
        // Validate version number uniqueness within budget
        if (budgetVersionRepository.existsByBudgetIdAndVersionNumber(
                budgetVersion.getBudgetId(), budgetVersion.getVersionNumber())) {
            throw new BudgetValidationException("Version " + budgetVersion.getVersionNumber() + 
                " already exists for this budget");
        }
        
        // Set default values
        if (budgetVersion.getStatus() == null) {
            budgetVersion.setStatus(BudgetVersion.BudgetVersionStatus.DRAFT);
        }
        if (budgetVersion.getIsCurrent() == null) {
            budgetVersion.setIsCurrent(false);
        }
        if (budgetVersion.getIsBaseline() == null) {
            budgetVersion.setIsBaseline(false);
        }
        if (budgetVersion.getTotalAllocatedAmount() == null) {
            budgetVersion.setTotalAllocatedAmount(BigDecimal.ZERO);
        }
        if (budgetVersion.getTotalActualAmount() == null) {
            budgetVersion.setTotalActualAmount(BigDecimal.ZERO);
        }
        if (budgetVersion.getTotalVariance() == null) {
            budgetVersion.setTotalVariance(BigDecimal.ZERO);
        }
        
        BudgetVersion savedVersion = budgetVersionRepository.save(budgetVersion);
        log.info("Budget version created successfully: {}", savedVersion.getVersionNumber());
        return savedVersion;
    }

    @Transactional
    public BudgetLine createBudgetLine(@Valid BudgetLine budgetLine) {
        log.info("Creating budget line for account: {}", budgetLine.getAccountId());
        
        // Validate budget or budget version exists
        if (budgetLine.getBudgetId() != null && !budgetRepository.existsById(budgetLine.getBudgetId())) {
            throw new BudgetValidationException("Budget not found: " + budgetLine.getBudgetId());
        }
        if (budgetLine.getBudgetVersionId() != null && !budgetVersionRepository.existsById(budgetLine.getBudgetVersionId())) {
            throw new BudgetValidationException("Budget version not found: " + budgetLine.getBudgetVersionId());
        }
        if (budgetLine.getBudgetId() != null) {
            Budget budget = budgetRepository.findById(budgetLine.getBudgetId())
                    .orElseThrow(() -> new BudgetValidationException("Budget not found: " + budgetLine.getBudgetId()));
            assertBudgetUnlocked(budget);
        }
        
        // Set default values
        if (budgetLine.getAllocatedAmount() == null) {
            budgetLine.setAllocatedAmount(BigDecimal.ZERO);
        }
        if (budgetLine.getActualAmount() == null) {
            budgetLine.setActualAmount(BigDecimal.ZERO);
        }
        if (budgetLine.getCommitmentAmount() == null) {
            budgetLine.setCommitmentAmount(BigDecimal.ZERO);
        }
        if (budgetLine.getAvailableAmount() == null) {
            budgetLine.setAvailableAmount(BigDecimal.ZERO);
        }
        if (budgetLine.getVarianceAmount() == null) {
            budgetLine.setVarianceAmount(BigDecimal.ZERO);
        }
        if (budgetLine.getVariancePercentage() == null) {
            budgetLine.setVariancePercentage(BigDecimal.ZERO);
        }
        if (budgetLine.getBudgetPeriodType() == null) {
            budgetLine.setBudgetPeriodType("ANNUAL");
        }
        if (budgetLine.getSpreadMethod() == null) {
            budgetLine.setSpreadMethod("EVEN");
        }
        
        BudgetLine savedLine = budgetLineRepository.save(budgetLine);
        log.info("Budget line created successfully for account: {}", savedLine.getAccountId());
        return savedLine;
    }

    @Transactional
    public BudgetChange createBudgetChange(@Valid BudgetChange budgetChange) {
        log.info("Creating budget change: {} for budget line: {}", 
                budgetChange.getChangeType(), budgetChange.getBudgetLineId());
        
        // Validate budget line exists
        if (!budgetLineRepository.existsById(budgetChange.getBudgetLineId())) {
            throw new BudgetValidationException("Budget line not found: " + budgetChange.getBudgetLineId());
        }
        BudgetLine line = budgetLineRepository.findById(budgetChange.getBudgetLineId())
                .orElseThrow(() -> new BudgetValidationException("Budget line not found: " + budgetChange.getBudgetLineId()));
        Budget budget = budgetRepository.findById(line.getBudgetId())
                .orElseThrow(() -> new BudgetValidationException("Budget not found: " + line.getBudgetId()));
        assertBudgetUnlocked(budget);
        
        // Set default values
        if (budgetChange.getStatus() == null) {
            budgetChange.setStatus(BudgetChange.ChangeStatus.PENDING);
        }
        
        BudgetChange savedChange = budgetChangeRepository.save(budgetChange);
        log.info("Budget change created successfully: {}", savedChange.getChangeType());
        return savedChange;
    }

    @Transactional
    public Budget approveBudget(UUID budgetId, String approvedBy) {
        log.info("Approving budget: {}", budgetId);
        
        Optional<Budget> budgetOpt = budgetRepository.findById(budgetId);
        if (budgetOpt.isEmpty()) {
            throw new BudgetValidationException("Budget not found: " + budgetId);
        }
        
        Budget budget = budgetOpt.get();
        assertBudgetUnlocked(budget);
        
        if (budget.getStatus() != Budget.BudgetStatus.SUBMITTED && budget.getStatus() != Budget.BudgetStatus.UNDER_REVIEW) {
            throw new BudgetValidationException("Budget must be submitted or under review to be approved");
        }
        
        if (budget.getLocked()) {
            throw new BudgetValidationException("Budget is locked and cannot be approved");
        }
        
        budget.setStatus(Budget.BudgetStatus.APPROVED);
        budget.setApprovedAt(LocalDate.now());
        budget.setApprovedBy(approvedBy);
        
        Budget updatedBudget = budgetRepository.save(budget);
        financeEventService.publishBudgetUpdatedEvent(updatedBudget.getId(), updatedBudget.getTenantId(), approvedBy);
        log.info("Budget approved successfully: {}", updatedBudget.getBudgetName());
        return updatedBudget;
    }

    @Transactional
    public Budget lockBudget(UUID budgetId, String lockedBy) {
        log.info("Locking budget: {}", budgetId);
        
        Optional<Budget> budgetOpt = budgetRepository.findById(budgetId);
        if (budgetOpt.isEmpty()) {
            throw new BudgetValidationException("Budget not found: " + budgetId);
        }
        
        Budget budget = budgetOpt.get();
        
        if (budget.getStatus() != Budget.BudgetStatus.APPROVED) {
            throw new BudgetValidationException("Only approved budgets can be locked");
        }
        
        budget.setLocked(true);
        budget.setLockedAt(LocalDate.now());
        budget.setLockedBy(lockedBy);
        
        Budget updatedBudget = budgetRepository.save(budget);
        financeEventService.publishBudgetUpdatedEvent(updatedBudget.getId(), updatedBudget.getTenantId(), lockedBy);
        log.info("Budget locked successfully: {}", updatedBudget.getBudgetName());
        return updatedBudget;
    }

    @Transactional
    public BudgetVersion setCurrentBudgetVersion(UUID budgetVersionId) {
        log.info("Setting current budget version: {}", budgetVersionId);
        
        Optional<BudgetVersion> versionOpt = budgetVersionRepository.findById(budgetVersionId);
        if (versionOpt.isEmpty()) {
            throw new BudgetValidationException("Budget version not found: " + budgetVersionId);
        }
        
        BudgetVersion newCurrentVersion = versionOpt.get();
        
        // Clear current flag from all versions of this budget
        List<BudgetVersion> allVersions = budgetVersionRepository.findByBudgetIdOrderByVersionNumberDesc(newCurrentVersion.getBudgetId());
        allVersions.forEach(version -> version.setIsCurrent(false));
        budgetVersionRepository.saveAll(allVersions);
        
        // Set new current version
        newCurrentVersion.setIsCurrent(true);
        BudgetVersion updatedVersion = budgetVersionRepository.save(newCurrentVersion);
        
        log.info("Current budget version set successfully: {}", updatedVersion.getVersionNumber());
        return updatedVersion;
    }

    @Transactional(readOnly = true)
    public Optional<Budget> getBudgetById(UUID budgetId) {
        return budgetRepository.findById(budgetId);
    }

    @Transactional(readOnly = true)
    public List<Budget> getBudgetsByFiscalYear(UUID fiscalYearId) {
        return budgetRepository.findByTenantIdAndFiscalYearId(
            com.financial.corefinance.domain.base.TenantContext.getCurrentTenant(), fiscalYearId);
    }

    @Transactional(readOnly = true)
    public List<Budget> getBudgetsByStatus(Budget.BudgetStatus status) {
        return budgetRepository.findByTenantIdAndStatus(
            com.financial.corefinance.domain.base.TenantContext.getCurrentTenant(), status);
    }

    @Transactional(readOnly = true)
    public List<BudgetVersion> getBudgetVersions(UUID budgetId) {
        return budgetVersionRepository.findByBudgetIdOrderByVersionNumberDesc(budgetId);
    }

    @Transactional(readOnly = true)
    public List<BudgetLine> getBudgetLines(UUID budgetId, UUID budgetVersionId) {
        if (budgetVersionId != null) {
            return budgetLineRepository.findByBudgetVersionId(budgetVersionId);
        } else {
            return budgetLineRepository.findByBudgetId(budgetId);
        }
    }

    @Transactional(readOnly = true)
    public List<BudgetChange> getBudgetChanges(UUID budgetVersionId) {
        return budgetChangeRepository.findByBudgetVersionIdOrderByCreatedAtDesc(budgetVersionId);
    }

    @Transactional
    public BudgetLine updateBudgetLineActualAmount(UUID budgetLineId, BigDecimal actualAmount) {
        log.info("Updating actual amount for budget line: {} to: {}", budgetLineId, actualAmount);
        
        Optional<BudgetLine> lineOpt = budgetLineRepository.findById(budgetLineId);
        if (lineOpt.isEmpty()) {
            throw new BudgetValidationException("Budget line not found: " + budgetLineId);
        }
        
        BudgetLine budgetLine = lineOpt.get();
        BigDecimal oldAmount = budgetLine.getActualAmount();
        budgetLine.setActualAmount(actualAmount);
        
        // Calculate variance and available amount
        budgetLine.calculateAmounts();
        
        BudgetLine updatedLine = budgetLineRepository.save(budgetLine);
        
        // Update budget totals
        updateBudgetTotals(budgetLine.getBudgetId(), budgetLine.getBudgetVersionId());
        
        log.info("Budget line actual amount updated successfully. Old: {}, New: {}", oldAmount, actualAmount);
        return updatedLine;
    }

    @Transactional
    public BudgetChange approveBudgetChange(UUID budgetChangeId, String approvedBy) {
        log.info("Approving budget change: {}", budgetChangeId);
        
        Optional<BudgetChange> changeOpt = budgetChangeRepository.findById(budgetChangeId);
        if (changeOpt.isEmpty()) {
            throw new BudgetValidationException("Budget change not found: " + budgetChangeId);
        }
        
        BudgetChange budgetChange = changeOpt.get();
        
        if (budgetChange.getStatus() != BudgetChange.ChangeStatus.PENDING) {
            throw new BudgetValidationException("Only pending budget changes can be approved");
        }
        
        budgetChange.setStatus(BudgetChange.ChangeStatus.APPROVED);
        budgetChange.setApprovedBy(approvedBy);
        budgetChange.setApprovedAt(java.time.LocalDateTime.now());
        
        // Apply the change to the budget line
        applyBudgetChange(budgetChange);
        
        BudgetChange updatedChange = budgetChangeRepository.save(budgetChange);
        log.info("Budget change approved and applied successfully: {}", updatedChange.getChangeType());
        return updatedChange;
    }

    private void updateBudgetTotals(UUID budgetId, UUID budgetVersionId) {
        // Update budget totals based on budget lines
        List<BudgetLine> budgetLines;
        if (budgetVersionId != null) {
            budgetLines = budgetLineRepository.findByBudgetVersionId(budgetVersionId);
            // Update budget version totals
            updateBudgetVersionTotals(budgetVersionId, budgetLines);
        } else {
            budgetLines = budgetLineRepository.findByBudgetId(budgetId);
            // Update budget totals
            updateBudgetMainTotals(budgetId, budgetLines);
        }
    }

    private void updateBudgetMainTotals(UUID budgetId, List<BudgetLine> budgetLines) {
        Optional<Budget> budgetOpt = budgetRepository.findById(budgetId);
        if (budgetOpt.isPresent()) {
            Budget budget = budgetOpt.get();
            
            BigDecimal totalBudgetAmount = budgetLines.stream()
                .filter(line -> line.getBudgetAmount() != null)
                .map(BudgetLine::getBudgetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalActualAmount = budgetLines.stream()
                .filter(line -> line.getActualAmount() != null)
                .map(BudgetLine::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            budget.setTotalBudgetAmount(totalBudgetAmount);
            budget.setTotalActualAmount(totalActualAmount);
            budget.calculateVariance();
            
            budgetRepository.save(budget);
        }
    }

    private void updateBudgetVersionTotals(UUID budgetVersionId, List<BudgetLine> budgetLines) {
        Optional<BudgetVersion> versionOpt = budgetVersionRepository.findById(budgetVersionId);
        if (versionOpt.isPresent()) {
            BudgetVersion version = versionOpt.get();
            
            BigDecimal totalBudgetAmount = budgetLines.stream()
                .filter(line -> line.getBudgetAmount() != null)
                .map(BudgetLine::getBudgetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalActualAmount = budgetLines.stream()
                .filter(line -> line.getActualAmount() != null)
                .map(BudgetLine::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            version.setTotalBudgetAmount(totalBudgetAmount);
            version.setTotalActualAmount(totalActualAmount);
            version.calculateVariance();
            
            budgetVersionRepository.save(version);
        }
    }

    private void applyBudgetChange(BudgetChange budgetChange) {
        Optional<BudgetLine> lineOpt = budgetLineRepository.findById(budgetChange.getBudgetLineId());
        if (lineOpt.isPresent()) {
            BudgetLine budgetLine = lineOpt.get();
            Budget budget = budgetRepository.findById(budgetLine.getBudgetId())
                    .orElseThrow(() -> new BudgetValidationException("Budget not found: " + budgetLine.getBudgetId()));
            assertBudgetUnlocked(budget);
            
            if (budgetChange.getNewAmount() != null) {
                budgetLine.setBudgetAmount(budgetChange.getNewAmount());
                budgetLine.calculateAmounts();
                budgetLineRepository.save(budgetLine);
                
                // Update totals
                updateBudgetTotals(budgetLine.getBudgetId(), budgetLine.getBudgetVersionId());
            }
        }
    }

    private void assertBudgetUnlocked(Budget budget) {
        if (Boolean.TRUE.equals(budget.getLocked())) {
            throw new BudgetValidationException("Budget is locked and cannot be modified");
        }
    }
}
