package com.finance.transactional.mapper;

import com.finance.transactional.dto.ReceiptAllocationDto;
import com.finance.transactional.model.ar.Receipt;
import com.finance.transactional.model.ar.ReceiptAllocation;
import com.finance.transactional.model.ar.SalesInvoice;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReceiptAllocationMapper {
    @Mapping(source = "receipt.id", target = "receiptId")
    @Mapping(source = "salesInvoice.id", target = "salesInvoiceId")
    ReceiptAllocationDto toDto(ReceiptAllocation entity);

    @Mapping(source = "receiptId", target = "receipt")
    @Mapping(source = "salesInvoiceId", target = "salesInvoice")
    ReceiptAllocation toEntity(ReceiptAllocationDto dto);

    default Receipt mapReceipt(UUID id) {
        if (id == null) {
            return null;
        }
        Receipt entity = new Receipt();
        entity.setId(id);
        return entity;
    }

    default SalesInvoice mapSalesInvoice(UUID id) {
        if (id == null) {
            return null;
        }
        SalesInvoice entity = new SalesInvoice();
        entity.setId(id);
        return entity;
    }
}
