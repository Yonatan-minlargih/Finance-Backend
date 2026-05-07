package com.finance.transactional.mapper;

import com.finance.transactional.dto.InvoiceDto;
import com.finance.transactional.model.ap.Invoice;
import com.finance.transactional.model.ap.PurchaseOrder;
import com.finance.transactional.model.ap.Vendor;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {InvoiceLineMapper.class})
public interface InvoiceMapper {
    @Mapping(source = "vendor.id", target = "vendorId")
    @Mapping(source = "purchaseOrder.id", target = "purchaseOrderId")
    InvoiceDto toDto(Invoice entity);

    @Mapping(source = "vendorId", target = "vendor")
    @Mapping(source = "purchaseOrderId", target = "purchaseOrder")
    Invoice toEntity(InvoiceDto dto);

    default Vendor mapVendor(UUID id) {
        if (id == null) {
            return null;
        }
        Vendor entity = new Vendor();
        entity.setId(id);
        return entity;
    }

    default PurchaseOrder mapPurchaseOrder(UUID id) {
        if (id == null) {
            return null;
        }
        PurchaseOrder entity = new PurchaseOrder();
        entity.setId(id);
        return entity;
    }
}
