package com.financial.corefinance.controller;

import com.financial.corefinance.domain.entity.Account;
import com.financial.corefinance.service.AccountService;
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
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chart of Accounts", description = "APIs for managing the chart of accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Create a new account", description = "Creates a new account in the chart of accounts")
    public ResponseEntity<Account> createAccount(@Valid @RequestBody Account account) {
        log.info("Creating account: {}", account.getAccountCode());
        
        Account savedAccount = accountService.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
    }

    @GetMapping("/{accountId}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get account by ID", description = "Retrieves an account by its ID")
    public ResponseEntity<Account> getAccount(
            @Parameter(description = "Account ID") @PathVariable UUID accountId) {
        log.info("Retrieving account with ID: {}", accountId);
        
        Optional<Account> account = accountService.getAccountById(accountId);
        return account.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{accountCode}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get account by code", description = "Retrieves an account by its code")
    public ResponseEntity<Account> getAccountByCode(
            @Parameter(description = "Account code") @PathVariable String accountCode) {
        log.info("Retrieving account with code: {}", accountCode);
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        Optional<Account> account = accountService.getAccountByCode(tenantId, accountCode);
        return account.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get all accounts", description = "Retrieves a paginated list of accounts")
    public ResponseEntity<Page<Account>> getAllAccounts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "accountCode") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<Account> allAccounts = accountService.getActiveAccounts(tenantId);
        Page<Account> accounts = new org.springframework.data.domain.PageImpl<>(allAccounts, pageable, allAccounts.size());
        
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Search accounts", description = "Searches accounts by code or name")
    public ResponseEntity<List<Account>> searchAccounts(
            @Parameter(description = "Search term") @RequestParam String search) {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<Account> accounts = accountService.searchAccounts(tenantId, search);
        
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/type/{accountType}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get accounts by type", description = "Retrieves accounts filtered by type")
    public ResponseEntity<List<Account>> getAccountsByType(
            @Parameter(description = "Account type") @PathVariable Account.AccountType accountType) {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<Account> accounts = accountService.getAccountsByType(tenantId, accountType);
        
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/ifrs-category/{ifrsCategory}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get accounts by IFRS category", description = "Retrieves accounts filtered by IFRS category")
    public ResponseEntity<List<Account>> getAccountsByIFRSCategory(
            @Parameter(description = "IFRS category") @PathVariable Account.IFRSCategory ifrsCategory) {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<Account> accounts = accountService.getAccountsByIFRSCategory(tenantId, ifrsCategory);
        
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/hierarchy")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get account hierarchy", description = "Retrieves the complete account hierarchy")
    public ResponseEntity<List<Account>> getAccountHierarchy() {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<Account> accounts = accountService.getAccountHierarchy(tenantId);
        
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/parent/{parentId}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get sub-accounts", description = "Retrieves sub-accounts for a parent account")
    public ResponseEntity<List<Account>> getSubAccounts(
            @Parameter(description = "Parent account ID") @PathVariable UUID parentId) {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<Account> accounts = accountService.getSubAccounts(tenantId, parentId);
        
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/manual-entry")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Get accounts for manual entry", description = "Retrieves accounts that allow manual entry")
    public ResponseEntity<List<Account>> getAccountsForManualEntry() {
        
        String tenantId = com.financial.corefinance.domain.base.TenantContext.getCurrentTenant();
        List<Account> accounts = accountService.getAccountsForManualEntry(tenantId);
        
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/{accountId}")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Update account", description = "Updates an existing account")
    public ResponseEntity<Account> updateAccount(
            @Parameter(description = "Account ID") @PathVariable UUID accountId,
            @Valid @RequestBody Account account) {
        log.info("Updating account with ID: {}", accountId);
        
        account.setId(accountId);
        Account updatedAccount = accountService.updateAccount(account);
        
        return ResponseEntity.ok(updatedAccount);
    }

    @DeleteMapping("/{accountId}")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Delete account", description = "Deletes an account (if not used in transactions)")
    public ResponseEntity<Void> deleteAccount(
            @Parameter(description = "Account ID") @PathVariable UUID accountId) {
        log.info("Deleting account with ID: {}", accountId);
        
        accountService.deleteAccount(accountId);
        
        return ResponseEntity.noContent().build();
    }
}
