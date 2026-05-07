package com.finance.transactional.mapper;

import com.finance.transactional.dto.VendorAddressDto;
import com.finance.transactional.model.ap.Vendor;
import com.finance.transactional.model.ap.VendorAddress;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VendorAddressMapper {
    @Mapping(source = "vendor.id", target = "vendorId")
    VendorAddressDto toDto(VendorAddress entity);

    @Mapping(source = "vendorId", target = "vendor")
    VendorAddress toEntity(VendorAddressDto dto);

    default Vendor mapVendor(UUID id) {
        if (id == null) {
            return null;
        }
        Vendor entity = new Vendor();
        entity.setId(id);
        return entity;
    }
}
