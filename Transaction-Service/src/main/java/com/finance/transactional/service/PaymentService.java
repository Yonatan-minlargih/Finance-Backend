package com.finance.transactional.service;

import com.finance.transactional.dto.PaymentDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.ap.Payment;
import com.finance.transactional.event.DomainEventPublisher;
import com.finance.transactional.mapper.PaymentMapper;
import com.finance.transactional.repository.PaymentRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public PaymentDto createPayment(UUID tenantId, PaymentDto dto) {
        Payment payment = mapper.toEntity(dto);
        payment.setTenantId(tenantId);
        Payment saved = repository.save(payment);
        PaymentDto resultDto = mapper.toDto(saved);

        // Publish event
        domainEventPublisher.publish("payment-posted", resultDto);

        return resultDto;
    }

    @Transactional
    public PaymentDto updatePayment(UUID tenantId, UUID id, PaymentDto dto) {
        Payment existing = getExistingPayment(tenantId, id);
        Payment updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingPayment(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getAllPayments(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deletePayment(UUID tenantId, UUID id) {
        Payment payment = getExistingPayment(tenantId, id);
        repository.delete(payment);
    }

    private Payment getExistingPayment(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id " + id));
    }
}
