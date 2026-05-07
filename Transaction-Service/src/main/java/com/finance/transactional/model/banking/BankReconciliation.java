package com.finance.transactional.model.banking;

import com.finance.transactional.model.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bank_reconciliations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BankReconciliation extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @Column(name = "statement_date", nullable = false)
    private LocalDate statementDate;

    @Column(name = "statement_balance", precision = 15, scale = 2)
    private BigDecimal statementBalance;

    @Column(name = "system_balance", precision = 15, scale = 2)
    private BigDecimal systemBalance;

    @Column(name = "variance", precision = 15, scale = 2)
    private BigDecimal variance;

    @Column(name = "status", length = 50)
    private String status; // DRAFT, COMPLETED
}
