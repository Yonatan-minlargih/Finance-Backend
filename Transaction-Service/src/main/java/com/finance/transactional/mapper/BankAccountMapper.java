package com.finance.transactional.mapper;

import com.finance.transactional.model.banking.BankAccount;
import com.finance.transactional.dto.BankAccountDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BankAccountMapper {
    BankAccountDto toDto(BankAccount entity);
    BankAccount toEntity(BankAccountDto dto);
}
