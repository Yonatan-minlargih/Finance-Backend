package com.finance.transactional.mapper;

import com.finance.transactional.dto.VendorDto;
import com.finance.transactional.model.ap.Vendor;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {VendorAddressMapper.class})
public interface VendorMapper {
    VendorDto toDto(Vendor entity);

    Vendor toEntity(VendorDto dto);

}
