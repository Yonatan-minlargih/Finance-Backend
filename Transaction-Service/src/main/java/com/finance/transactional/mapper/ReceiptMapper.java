package com.finance.transactional.mapper;

import com.finance.transactional.dto.ReceiptDto;
import com.finance.transactional.model.ar.Customer;
import com.finance.transactional.model.ar.Receipt;
import com.finance.transactional.model.banking.BankAccount;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {ReceiptAllocationMapper.class})
public interface ReceiptMapper {
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "bankAccount.id", target = "bankAccountId")
    ReceiptDto toDto(Receipt entity);

    @Mapping(source = "customerId", target = "customer")
    @Mapping(source = "bankAccountId", target = "bankAccount")
    Receipt toEntity(ReceiptDto dto);

    default Customer mapCustomer(UUID id) {
        if (id == null) {
            return null;
        }
        Customer entity = new Customer();
        entity.setId(id);
        return entity;
    }

    default BankAccount mapBankAccount(UUID id) {
        if (id == null) {
            return null;
        }
        BankAccount entity = new BankAccount();
        entity.setId(id);
        return entity;
    }
}
