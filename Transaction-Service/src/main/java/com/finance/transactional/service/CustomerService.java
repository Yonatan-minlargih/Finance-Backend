package com.finance.transactional.service;

import com.finance.transactional.dto.CustomerDto;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.model.ar.Customer;
import com.finance.transactional.mapper.CustomerMapper;
import com.finance.transactional.repository.CustomerRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    @Transactional
    public CustomerDto createCustomer(UUID tenantId, CustomerDto dto) {
        Customer customer = mapper.toEntity(dto);
        customer.setTenantId(tenantId);
        Customer saved = repository.save(customer);
        return mapper.toDto(saved);
    }

    @Transactional
    public CustomerDto updateCustomer(UUID tenantId, UUID id, CustomerDto dto) {
        Customer existing = getExistingCustomer(tenantId, id);
        Customer updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setTenantId(tenantId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated = repository.save(updated);
        return mapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingCustomer(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<CustomerDto> getAllCustomers(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteCustomer(UUID tenantId, UUID id) {
        Customer customer = getExistingCustomer(tenantId, id);
        repository.delete(customer);
    }

    private Customer getExistingCustomer(UUID tenantId, UUID id) {
        return repository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
    }
}
