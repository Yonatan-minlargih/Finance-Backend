package com.finance.transactional.mapper;

import com.finance.transactional.dto.PurchaseOrderDto;
import com.finance.transactional.model.ap.PurchaseOrder;
import com.finance.transactional.model.ap.Vendor;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PurchaseOrderMapper {
    @Mapping(source = "vendor.id", target = "vendorId")
    PurchaseOrderDto toDto(PurchaseOrder entity);

    @Mapping(source = "vendorId", target = "vendor")
    PurchaseOrder toEntity(PurchaseOrderDto dto);

    default Vendor mapVendor(UUID id) {
        if (id == null) {
            return null;
        }
        Vendor entity = new Vendor();
        entity.setId(id);
        return entity;
    }
}
