package com.financial.corefinance.service;

import com.financial.corefinance.config.IntegrationGlAccountProperties;
import com.financial.corefinance.config.IntegrationGlAccountProperties.AccountDefinition;
import com.financial.corefinance.domain.entity.Account;
import com.financial.corefinance.repository.AccountRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationGlAccountService {

    private final IntegrationGlAccountProperties properties;
    private final AccountRepository accountRepository;

    @Transactional
    public List<SeedResult> seedIntegrationChart(String tenantId) {
        List<SeedResult> results = new ArrayList<>();
        if (!properties.isEnabled()) {
            return results;
        }
        for (var entry : properties.getAccounts().entrySet()) {
            results.add(ensureAccount(tenantId, entry.getKey(), entry.getValue()));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public Account resolveByKey(String tenantId, String accountKey) {
        AccountDefinition def = properties.getAccounts().get(accountKey);
        if (def == null) {
            throw new IllegalArgumentException("Unknown integration account key: " + accountKey);
        }
        return resolveByDefinition(tenantId, accountKey, def);
    }

    @Transactional(readOnly = true)
    public Account resolveByIdOrCode(String tenantId, String accountIdOrCode) {
        try {
            UUID accountId = UUID.fromString(accountIdOrCode.trim());
            Account byId = accountRepository
                    .findById(accountId)
                    .filter(a -> tenantId.equals(a.getTenantId()))
                    .orElseThrow(() -> new IllegalArgumentException("GL account not found: " + accountId));
            ensureActive(byId);
            return byId;
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage() != null && ex.getMessage().startsWith("GL account not found")) {
                throw ex;
            }
            if (ex.getMessage() != null && ex.getMessage().startsWith("Account is not active")) {
                throw ex;
            }
            return resolveByCode(tenantId, accountIdOrCode);
        }
    }

    @Transactional(readOnly = true)
    public Account resolveByCode(String tenantId, String accountCode) {
        if (accountCode == null || accountCode.isBlank()) {
            return resolveByKey(tenantId, com.financial.corefinance.integration.IntegrationAccountKeys.AP_EXPENSE_DEFAULT);
        }
        String code = accountCode.trim();
        Optional<AccountDefinition> catalogMatch = properties.getAccounts().values().stream()
                .filter(def -> code.equals(def.getCode()))
                .findFirst();
        if (catalogMatch.isPresent()) {
            String key = properties.getAccounts().entrySet().stream()
                    .filter(e -> code.equals(e.getValue().getCode()))
                    .map(java.util.Map.Entry::getKey)
                    .findFirst()
                    .orElse(code);
            return resolveByDefinition(tenantId, key, catalogMatch.get());
        }
        return accountRepository
                .findByTenantIdAndAccountCode(tenantId, code)
                .map(acc -> {
                    if (catalogMatch.isPresent()) {
                        String key = properties.getAccounts().entrySet().stream()
                                .filter(e -> code.equals(e.getValue().getCode()))
                                .map(java.util.Map.Entry::getKey)
                                .findFirst()
                                .orElse(code);
                        return activateIfNeeded(tenantId, key, catalogMatch.get(), acc);
                    }
                    ensureActive(acc);
                    return acc;
                })
                .orElseThrow(() -> new IllegalArgumentException(
                        "GL account not found: " + code + ". Add it in Chart of Accounts or run seed-integration-chart."));
    }

    public String codeForKey(String accountKey) {
        AccountDefinition def = properties.getAccounts().get(accountKey);
        if (def == null) {
            throw new IllegalArgumentException("Unknown integration account key: " + accountKey);
        }
        return def.getCode();
    }

    private Account resolveByDefinition(String tenantId, String accountKey, AccountDefinition def) {
        return accountRepository
                .findByTenantIdAndAccountCode(tenantId, def.getCode())
                .map(acc -> activateIfNeeded(tenantId, accountKey, def, acc))
                .orElseGet(() -> createAccount(tenantId, accountKey, def));
    }

    private SeedResult ensureAccount(String tenantId, String accountKey, AccountDefinition def) {
        Optional<Account> existing = accountRepository.findByTenantIdAndAccountCode(tenantId, def.getCode());
        if (existing.isEmpty()) {
            Account created = createAccount(tenantId, accountKey, def);
            return SeedResult.created(created);
        }
        Account acc = existing.get();
        if (!matchesExpectedType(acc, def)) {
            return SeedResult.conflict(
                    def.getCode(),
                    "Account exists with type "
                            + acc.getAccountType()
                            + " but integration expects "
                            + def.getAccountType()
                            + ". Use a different code in integration-gl-accounts.yml or fix the COA.");
        }
        if (!Boolean.TRUE.equals(acc.getIsActive())
                || acc.getAllowManualEntry() == null
                || !Boolean.TRUE.equals(acc.getAllowManualEntry())) {
            Account updated = activateIfNeeded(tenantId, accountKey, def, acc);
            return SeedResult.reactivated(updated);
        }
        return SeedResult.ok(acc);
    }

    private Account activateIfNeeded(String tenantId, String accountKey, AccountDefinition def, Account acc) {
        boolean changed = false;
        if (!Boolean.TRUE.equals(acc.getIsActive())) {
            acc.setIsActive(true);
            changed = true;
            log.warn("Reactivated integration account {} ({}) for tenant {}", def.getCode(), accountKey, tenantId);
        }
        if (acc.getAllowManualEntry() == null || !Boolean.TRUE.equals(acc.getAllowManualEntry())) {
            acc.setAllowManualEntry(Boolean.TRUE.equals(def.getAllowManualEntry()));
            changed = true;
        }
        if (acc.getIsConsolidated() == null) {
            acc.setIsConsolidated(false);
            changed = true;
        }
        if (acc.getCurrencyCode() == null || acc.getCurrencyCode().isBlank()) {
            acc.setCurrencyCode(properties.getBaseCurrency());
            changed = true;
        }
        return changed ? accountRepository.save(acc) : acc;
    }

    private Account createAccount(String tenantId, String accountKey, AccountDefinition def) {
        log.info("Creating integration GL account {} — {} [{}]", def.getCode(), def.getName(), accountKey);
        Account account = new Account();
        account.setTenantId(tenantId);
        account.setAccountCode(def.getCode());
        account.setAccountName(def.getName());
        account.setAccountType(parseAccountType(def.getAccountType()));
        account.setNormalBalance(parseNormalBalance(def.getNormalBalance(), account.getAccountType()));
        account.setIFRSCategory(parseIfrsCategory(def.getIfrsCategory()));
        account.setDescription(def.getDescription());
        account.setIsActive(true);
        account.setAllowManualEntry(Boolean.TRUE.equals(def.getAllowManualEntry()));
        account.setIsConsolidated(false);
        account.setCurrencyCode(properties.getBaseCurrency());
        return accountRepository.save(account);
    }

    private void ensureActive(Account acc) {
        if (!Boolean.TRUE.equals(acc.getIsActive())) {
            throw new IllegalArgumentException(
                    "Account is not active: "
                            + acc.getAccountCode()
                            + ". Run POST /api/v1/accounts/seed-integration-chart or activate the account.");
        }
    }

    private boolean matchesExpectedType(Account acc, AccountDefinition def) {
        try {
            return acc.getAccountType() == parseAccountType(def.getAccountType());
        } catch (Exception ex) {
            return false;
        }
    }

    private Account.AccountType parseAccountType(String raw) {
        return Account.AccountType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

    private Account.NormalBalance parseNormalBalance(String raw, Account.AccountType type) {
        if (raw != null && !raw.isBlank()) {
            return Account.NormalBalance.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        }
        return type == Account.AccountType.EXPENSE || type == Account.AccountType.ASSET
                ? Account.NormalBalance.DEBIT
                : Account.NormalBalance.CREDIT;
    }

    private Account.IFRSCategory parseIfrsCategory(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Account.IFRSCategory.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public record SeedResult(String accountCode, String status, String message) {
        static SeedResult created(Account a) {
            return new SeedResult(a.getAccountCode(), "CREATED", a.getAccountName());
        }

        static SeedResult reactivated(Account a) {
            return new SeedResult(a.getAccountCode(), "REACTIVATED", a.getAccountName());
        }

        static SeedResult ok(Account a) {
            return new SeedResult(a.getAccountCode(), "OK", a.getAccountName());
        }

        static SeedResult conflict(String code, String message) {
            return new SeedResult(code, "CONFLICT", message);
        }
    }
}
