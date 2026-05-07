package com.finance.transactional.mapper;

import com.finance.transactional.dto.PaymentDto;
import com.finance.transactional.model.ap.Payment;
import com.finance.transactional.model.ap.Vendor;
import com.finance.transactional.model.banking.BankAccount;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {PaymentAllocationMapper.class})
public interface PaymentMapper {
    @Mapping(source = "vendor.id", target = "vendorId")
    @Mapping(source = "bankAccount.id", target = "bankAccountId")
    PaymentDto toDto(Payment entity);

    @Mapping(source = "vendorId", target = "vendor")
    @Mapping(source = "bankAccountId", target = "bankAccount")
    Payment toEntity(PaymentDto dto);

    default Vendor mapVendor(UUID id) {
        if (id == null) {
            return null;
        }
        Vendor entity = new Vendor();
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
