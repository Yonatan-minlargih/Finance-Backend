package com.finance.transactional.mapper;

import com.finance.transactional.dto.BankTransactionDto;
import com.finance.transactional.model.banking.BankAccount;
import com.finance.transactional.model.banking.BankReconciliation;
import com.finance.transactional.model.banking.BankTransaction;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BankTransactionMapper {
    @Mapping(source = "bankAccount.id", target = "bankAccountId")
    @Mapping(source = "bankReconciliation.id", target = "bankReconciliationId")
    BankTransactionDto toDto(BankTransaction entity);

    @Mapping(source = "bankAccountId", target = "bankAccount")
    @Mapping(source = "bankReconciliationId", target = "bankReconciliation")
    BankTransaction toEntity(BankTransactionDto dto);

    default BankAccount mapBankAccount(UUID id) {
        if (id == null) {
            return null;
        }
        BankAccount entity = new BankAccount();
        entity.setId(id);
        return entity;
    }

    default BankReconciliation mapBankReconciliation(UUID id) {
        if (id == null) {
            return null;
        }
        BankReconciliation entity = new BankReconciliation();
        entity.setId(id);
        return entity;
    }
}
