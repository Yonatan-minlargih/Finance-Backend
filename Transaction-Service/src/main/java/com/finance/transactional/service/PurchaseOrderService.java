package com.finance.transactional.service;

import com.finance.transactional.dto.PurchaseOrderDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.ap.PurchaseOrder;
import com.finance.transactional.event.DomainEventPublisher;
import com.finance.transactional.mapper.PurchaseOrderMapper;
import com.finance.transactional.repository.PurchaseOrderRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository repository;
    private final PurchaseOrderMapper mapper;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public PurchaseOrderDto createPurchaseOrder(UUID tenantId, PurchaseOrderDto dto) {
        PurchaseOrder purchaseOrder = mapper.toEntity(dto);
        purchaseOrder.setTenantId(tenantId);
        PurchaseOrder saved = repository.save(purchaseOrder);
        PurchaseOrderDto resultDto = mapper.toDto(saved);

        // Publish event
        domainEventPublisher.publish("purchase-order-created", resultDto);

        return resultDto;
    }

    @Transactional
    public PurchaseOrderDto updatePurchaseOrder(UUID tenantId, UUID id, PurchaseOrderDto dto) {
        PurchaseOrder existing = getExistingPurchaseOrder(tenantId, id);
        PurchaseOrder updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDto getPurchaseOrderById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingPurchaseOrder(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderDto> getAllPurchaseOrders(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deletePurchaseOrder(UUID tenantId, UUID id) {
        PurchaseOrder purchaseOrder = getExistingPurchaseOrder(tenantId, id);
        repository.delete(purchaseOrder);
    }

    private PurchaseOrder getExistingPurchaseOrder(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder not found with id " + id));
    }
}
