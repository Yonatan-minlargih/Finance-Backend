package com.financial.corefinance.service;

import com.financial.corefinance.domain.entity.Account;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class GlBalanceSignService {

    /**
     * True when the signed balance is opposite the account's normal side
     * (e.g. credit balance on a debit-normal asset).
     */
    public boolean isAbnormalBalance(Account account, BigDecimal signedBalance) {
        if (account == null || signedBalance == null || signedBalance.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        Account.NormalBalance normal = account.getNormalBalance();
        if (normal == Account.NormalBalance.DEBIT) {
            return signedBalance.compareTo(BigDecimal.ZERO) < 0;
        }
        if (normal == Account.NormalBalance.CREDIT) {
            return signedBalance.compareTo(BigDecimal.ZERO) > 0;
        }
        return false;
    }

    public String abnormalBalanceReason(Account account, BigDecimal signedBalance) {
        if (!isAbnormalBalance(account, signedBalance)) {
            return null;
        }
        String code = account.getAccountCode() != null ? account.getAccountCode() : "";
        if ("1200".equals(code)) {
            return "AR is credit-balanced — usually missing sales invoice accrual (Dr AR / Cr Revenue) before collections.";
        }
        if ("2100".equals(code)) {
            return "AP is debit-balanced — usually missing supplier bill accrual (Dr Expense / Cr AP) before payments.";
        }
        if (account.getNormalBalance() == Account.NormalBalance.DEBIT) {
            return "Asset/expense account shows a credit balance — check for missing opening or source entries.";
        }
        return "Liability/revenue/equity account shows a debit balance — check for missing accrual before settlements.";
    }
}
