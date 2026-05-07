package com.finance.transactional.mapper;

import com.finance.transactional.dto.InvoiceLineDto;
import com.finance.transactional.model.ap.Invoice;
import com.finance.transactional.model.ap.InvoiceLine;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InvoiceLineMapper {
    @Mapping(source = "invoice.id", target = "invoiceId")
    InvoiceLineDto toDto(InvoiceLine entity);

    @Mapping(source = "invoiceId", target = "invoice")
    InvoiceLine toEntity(InvoiceLineDto dto);

    default Invoice mapInvoice(UUID id) {
        if (id == null) {
            return null;
        }
        Invoice entity = new Invoice();
        entity.setId(id);
        return entity;
    }
}
