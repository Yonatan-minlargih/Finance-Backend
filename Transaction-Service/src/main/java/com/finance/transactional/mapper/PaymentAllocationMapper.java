package com.finance.transactional.mapper;

import com.finance.transactional.dto.PaymentAllocationDto;
import com.finance.transactional.model.ap.Invoice;
import com.finance.transactional.model.ap.Payment;
import com.finance.transactional.model.ap.PaymentAllocation;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentAllocationMapper {
    @Mapping(source = "payment.id", target = "paymentId")
    @Mapping(source = "invoice.id", target = "invoiceId")
    PaymentAllocationDto toDto(PaymentAllocation entity);

    @Mapping(source = "paymentId", target = "payment")
    @Mapping(source = "invoiceId", target = "invoice")
    PaymentAllocation toEntity(PaymentAllocationDto dto);

    default Payment mapPayment(UUID id) {
        if (id == null) {
            return null;
        }
        Payment entity = new Payment();
        entity.setId(id);
        return entity;
    }

    default Invoice mapInvoice(UUID id) {
        if (id == null) {
            return null;
        }
        Invoice entity = new Invoice();
        entity.setId(id);
        return entity;
    }
}
