package com.finance.transactional.model.banking;

import com.finance.transactional.model.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "bank_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BankTransaction extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 50)
    private TransactionType transactionType;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount; // Positive = IN, Negative = OUT

    @Column(name = "reference_number", length = 100)
    private String referenceNumber; // e.g. Cheque No, Transfer Ref

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_reconciled")
    private Boolean isReconciled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_reconciliation_id")
    private BankReconciliation bankReconciliation;

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER, FEE, INTEREST
    }
}
