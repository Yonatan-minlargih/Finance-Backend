package com.finance.transactional.mapper;

import com.finance.transactional.dto.SalesInvoiceDto;
import com.finance.transactional.model.ar.Customer;
import com.finance.transactional.model.ar.SalesInvoice;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SalesInvoiceMapper {
    @Mapping(source = "customer.id", target = "customerId")
    SalesInvoiceDto toDto(SalesInvoice entity);

    @Mapping(source = "customerId", target = "customer")
    SalesInvoice toEntity(SalesInvoiceDto dto);

    default Customer mapCustomer(UUID id) {
        if (id == null) {
            return null;
        }
        Customer entity = new Customer();
        entity.setId(id);
        return entity;
    }
}
