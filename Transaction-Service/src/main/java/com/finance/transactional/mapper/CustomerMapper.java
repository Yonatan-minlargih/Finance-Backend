package com.finance.transactional.mapper;

import com.finance.transactional.model.ar.Customer;
import com.finance.transactional.dto.CustomerDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerMapper {
    CustomerDto toDto(Customer entity);
    Customer toEntity(CustomerDto dto);
}
