package com.financial.corefinance.mapper;

import com.financial.corefinance.domain.entity.Account;
import com.financial.corefinance.domain.entity.JournalLine;
import com.financial.corefinance.dto.response.JournalLineResponse;
import com.financial.corefinance.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JournalLineAccountEnricher {

    private final AccountRepository accountRepository;

    @AfterMapping
    public void enrichAccountFields(JournalLine line, @MappingTarget JournalLineResponse response) {
        if (line == null || response == null) {
            return;
        }
        response.setAccountId(line.getAccountId());

        Account account = line.getAccount();
        if (account == null && line.getAccountId() != null) {
            account = accountRepository.findById(line.getAccountId()).orElse(null);
        }

        if (account != null) {
            response.setAccountCode(account.getAccountCode());
            response.setAccountName(account.getAccountName());
        } else if (line.getAccountId() != null) {
            response.setAccountCode("—");
            response.setAccountName("Missing account (" + line.getAccountId() + ")");
        }
    }
}
