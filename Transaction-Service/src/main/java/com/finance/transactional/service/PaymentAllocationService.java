package com.finance.transactional.service;

import com.finance.transactional.dto.PaymentAllocationDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.ap.PaymentAllocation;
import com.finance.transactional.mapper.PaymentAllocationMapper;
import com.finance.transactional.repository.PaymentAllocationRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentAllocationService {

    private final PaymentAllocationRepository repository;
    private final PaymentAllocationMapper mapper;

    @Transactional
    public PaymentAllocationDto createPaymentAllocation(UUID tenantId, PaymentAllocationDto dto) {
        PaymentAllocation paymentAllocation = mapper.toEntity(dto);
        paymentAllocation.setTenantId(tenantId);
        PaymentAllocation saved = repository.save(paymentAllocation);
        return mapper.toDto(saved);
    }

    @Transactional
    public PaymentAllocationDto updatePaymentAllocation(UUID tenantId, UUID id, PaymentAllocationDto dto) {
        PaymentAllocation existing = getExistingPaymentAllocation(tenantId, id);
        PaymentAllocation updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public PaymentAllocationDto getPaymentAllocationById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingPaymentAllocation(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<PaymentAllocationDto> getAllPaymentAllocations(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deletePaymentAllocation(UUID tenantId, UUID id) {
        PaymentAllocation paymentAllocation = getExistingPaymentAllocation(tenantId, id);
        repository.delete(paymentAllocation);
    }

    private PaymentAllocation getExistingPaymentAllocation(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentAllocation not found with id " + id));
    }
}
