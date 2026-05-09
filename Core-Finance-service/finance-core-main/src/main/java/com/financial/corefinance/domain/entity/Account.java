package com.financial.corefinance.domain.entity;

import com.financial.corefinance.domain.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_accounts_tenant", columnList = "tenant_id"),
    @Index(name = "idx_accounts_code", columnList = "account_code"),
    @Index(name = "idx_accounts_parent", columnList = "parent_account_id")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uq_accounts_tenant_code", columnNames = {"tenant_id", "account_code"})
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Account extends BaseEntity {

    @NotBlank(message = "Account code is required")
    @Column(name = "account_code", length = 50, nullable = false)
    private String accountCode;

    @NotBlank(message = "Account name is required")
    @Column(name = "account_name", length = 200, nullable = false)
    private String accountName;

    @NotNull(message = "Account type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @NotNull(message = "Normal balance is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "normal_balance", nullable = false, length = 10)
    private NormalBalance normalBalance;

    @Column(name = "parent_account_id")
    private UUID parentAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_account_id", insertable = false, updatable = false)
    private Account parentAccount;

    @Builder.Default
    @OneToMany(mappedBy = "parentAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Account> subAccounts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "ifrs_category", length = 50)
    private IFRSCategory ifrsCategory;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_consolidated")
    @Builder.Default
    private Boolean isConsolidated = false;

    @Column(name = "level")
    private Integer level;

    @Column(name = "account_format", length = 20)
    private String accountFormat;

    @Column(name = "min_length")
    private Integer minLength;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "allow_manual_entry")
    @Builder.Default
    private Boolean allowManualEntry = true;

    @Column(name = "reconciliation_required")
    @Builder.Default
    private Boolean reconciliationRequired = false;

    @Column(name = "opening_balance", precision = 19, scale = 4)
    private BigDecimal openingBalance;

    @Column(name = "opening_balance_date")
    private LocalDate openingBalanceDate;

    @Column(name = "currency_code", length = 3)
    @Builder.Default
    private String currencyCode = "USD";

    @PrePersist
    @PreUpdate
    public void calculateLevel() {
        if (parentAccount != null) {
            this.level = parentAccount.getLevel() != null ? parentAccount.getLevel() + 1 : 1;
        } else {
            this.level = 0;
        }
    }

    public enum AccountType {
        ASSET,
        LIABILITY,
        EQUITY,
        REVENUE,
        EXPENSE,
        GAIN,
        LOSS
    }

    public enum NormalBalance {
        DEBIT,
        CREDIT
    }

    public enum IFRSCategory {
        CURRENT_ASSETS,
        NON_CURRENT_ASSETS,
        CURRENT_LIABILITIES,
        NON_CURRENT_LIABILITIES,
        EQUITY,
        REVENUE,
        OTHER_INCOME,
        OPERATING_EXPENSES,
        OTHER_EXPENSES
    }

    public IFRSCategory getIFRSCategory() {
        return this.ifrsCategory;
    }

    public void setIFRSCategory(IFRSCategory ifrsCategory) {
        this.ifrsCategory = ifrsCategory;
    }
}
