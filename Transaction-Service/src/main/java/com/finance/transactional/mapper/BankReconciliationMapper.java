package com.finance.transactional.mapper;

import com.finance.transactional.dto.BankReconciliationDto;
import com.finance.transactional.model.banking.BankAccount;
import com.finance.transactional.model.banking.BankReconciliation;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BankReconciliationMapper {
    @Mapping(source = "bankAccount.id", target = "bankAccountId")
    BankReconciliationDto toDto(BankReconciliation entity);

    @Mapping(source = "bankAccountId", target = "bankAccount")
    BankReconciliation toEntity(BankReconciliationDto dto);

    default BankAccount mapBankAccount(UUID id) {
        if (id == null) {
            return null;
        }
        BankAccount entity = new BankAccount();
        entity.setId(id);
        return entity;
    }
}
