package com.finance.transactional.service;

import com.finance.transactional.dto.ReceiptDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.ar.Receipt;
import com.finance.transactional.event.DomainEventPublisher;
import com.finance.transactional.mapper.ReceiptMapper;
import com.finance.transactional.repository.ReceiptRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository repository;
    private final ReceiptMapper mapper;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public ReceiptDto createReceipt(UUID tenantId, ReceiptDto dto) {
        Receipt receipt = mapper.toEntity(dto);
        receipt.setTenantId(tenantId);
        Receipt saved = repository.save(receipt);
        ReceiptDto resultDto = mapper.toDto(saved);

        // Publish event
        domainEventPublisher.publish("receipt-created", resultDto);

        return resultDto;
    }

    @Transactional
    public ReceiptDto updateReceipt(UUID tenantId, UUID id, ReceiptDto dto) {
        Receipt existing = getExistingReceipt(tenantId, id);
        Receipt updated = mapper.toEntity(dto);

        existing.setReceiptNumber(updated.getReceiptNumber());
        if (updated.getCustomer() != null && updated.getCustomer().getId() != null) {
            existing.setCustomer(updated.getCustomer());
        }
        if (updated.getBankAccount() != null && updated.getBankAccount().getId() != null) {
            existing.setBankAccount(updated.getBankAccount());
        }
        existing.setReceiptDate(updated.getReceiptDate());
        existing.setAmount(updated.getAmount());
        existing.setPaymentMethod(updated.getPaymentMethod());
        existing.setReferenceNumber(updated.getReferenceNumber());
        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus());
        }

        Receipt saved = repository.save(existing);
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public ReceiptDto getReceiptById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingReceipt(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<ReceiptDto> getAllReceipts(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public ReceiptDto postReceipt(UUID tenantId, UUID id) {
        Receipt receipt = getExistingReceipt(tenantId, id);
        if (receipt.getStatus() != Receipt.ReceiptStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT receipts can be posted.");
        }
        receipt.setStatus(Receipt.ReceiptStatus.POSTED);
        Receipt saved = repository.save(receipt);
        ReceiptDto resultDto = mapper.toDto(saved);

        // Publish event for General Ledger integration
        domainEventPublisher.publish("receipt-posted", resultDto);

        return resultDto;
    }

    @Transactional
    public void deleteReceipt(UUID tenantId, UUID id) {
        Receipt receipt = getExistingReceipt(tenantId, id);
        repository.delete(receipt);
    }

    private Receipt getExistingReceipt(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with id " + id));
    }
}
