package com.finance.transactional.model.banking;

import com.finance.transactional.model.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount extends BaseTenantEntity {

    @Column(name = "account_code", length = 50, nullable = false)
    private String accountCode; // e.g. internal code

    @Column(name = "bank_name", length = 100, nullable = false)
    private String bankName;

    @Column(name = "account_number", length = 100, nullable = false)
    private String accountNumber;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "current_balance", precision = 15, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // GL Account for integration with CoreFinance
    @Column(name = "gl_account_id", length = 36)
    private String glAccountId;
}
