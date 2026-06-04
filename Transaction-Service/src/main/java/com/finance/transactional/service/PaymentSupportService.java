package com.finance.transactional.service;

import com.finance.transactional.dto.PaymentDto;
import com.finance.transactional.model.ap.Payment;
import com.finance.transactional.model.banking.BankAccount;
import com.finance.transactional.util.PaymentMethodRules;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PaymentSupportService {

    public void preparePayment(Payment payment, PaymentDto dto, UUID tenantId, ApCurrencyService currencyService) {
        String method = PaymentMethodRules.normalize(dto.getPaymentMethod());
        payment.setPaymentMethod(method);

        currencyService.applyVendorCurrencyToPayment(
                payment,
                tenantId,
                dto.getCurrency(),
                dto.getExchangeRate(),
                dto.getForeignAmount() != null ? dto.getForeignAmount() : dto.getAmount());

        if (PaymentMethodRules.requiresBankAccount(method)) {
            if (dto.getBankAccountId() == null) {
                throw new IllegalArgumentException("Bank account is required for " + method);
            }
            BankAccount bankAccount = new BankAccount();
            bankAccount.setId(dto.getBankAccountId());
            payment.setBankAccount(bankAccount);
        } else {
            payment.setBankAccount(null);
        }
    }
}
