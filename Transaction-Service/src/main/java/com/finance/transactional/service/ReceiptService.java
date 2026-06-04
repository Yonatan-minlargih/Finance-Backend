package com.finance.transactional.service;

import com.finance.transactional.dto.ReceiptAllocationDto;
import com.finance.transactional.dto.ReceiptDto;
import com.finance.transactional.client.NumberingSeriesClient;
import com.finance.transactional.dto.event.ArReceiptGlPostResult;
import com.finance.transactional.exception.ApInvoiceApprovalException;
import com.finance.transactional.exception.ResourceNotFoundException;
import com.finance.transactional.mapper.ReceiptMapper;
import com.finance.transactional.model.ar.Receipt;
import com.finance.transactional.model.ar.ReceiptAllocation;
import com.finance.transactional.model.ar.SalesInvoice;
import com.finance.transactional.repository.ReceiptRepository;
import com.finance.transactional.repository.SalesInvoiceRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptService {

    private final ReceiptRepository repository;
    private final ReceiptMapper mapper;
    private final NumberingSeriesClient numberingSeriesClient;
    private final ArReceiptApplicationService arReceiptApplicationService;
    private final ArReceiptValidationService arReceiptValidationService;
    private final ArReceiptGlPostingGateway arReceiptGlPostingGateway;
    private final SalesInvoiceRepository salesInvoiceRepository;

    @Transactional
    public ReceiptDto createReceipt(UUID tenantId, ReceiptDto dto) {
        Receipt receipt = mapper.toEntity(dto);
        receipt.setTenantId(tenantId);
        receipt.setStatus(Receipt.ReceiptStatus.DRAFT);

        if (receipt.getReceiptNumber() == null || receipt.getReceiptNumber().isBlank()) {
            try {
                Map<String, String> result = numberingSeriesClient.getNextNumber("RECEIPT");
                receipt.setReceiptNumber(result.get("nextNumber"));
            } catch (Exception e) {
                log.warn("Failed to fetch next receipt number: {}", e.getMessage());
            }
        }

        attachAllocations(receipt, tenantId, dto.getAllocations());
        Receipt saved = repository.save(receipt);
        return mapper.toDto(saved);
    }

    @Transactional
    public ReceiptDto updateReceipt(UUID tenantId, UUID id, ReceiptDto dto) {
        Receipt existing = getExistingReceipt(tenantId, id);
        if (existing.getStatus() != Receipt.ReceiptStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT receipts can be updated");
        }
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

        existing.getAllocations().clear();
        attachAllocations(existing, tenantId, dto.getAllocations());

        return mapper.toDto(repository.save(existing));
    }

    @Transactional(readOnly = true)
    public ReceiptDto getReceiptById(UUID tenantId, UUID id) {
        return mapper.toDto(getExistingReceipt(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<ReceiptDto> getAllReceipts(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream().map(mapper::toDto).toList();
    }

    @Transactional
    public ReceiptDto postReceipt(UUID tenantId, UUID id) {
        Receipt receipt = getExistingReceipt(tenantId, id);
        if (receipt.getStatus() != Receipt.ReceiptStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT receipts can be posted.");
        }

        List<ReceiptAllocationDto> allocationDtos = toAllocationDtos(receipt);
        arReceiptValidationService.validateReceiptCanPost(receipt, allocationDtos);
        arReceiptApplicationService.applyReceipt(receipt, allocationDtos);

        ArReceiptGlPostResult glResult;
        try {
            glResult = arReceiptGlPostingGateway.postReceiptAndWait(receipt);
        } catch (Exception ex) {
            throw new ApInvoiceApprovalException(
                    "Receipt posting failed: could not post to General Ledger. " + ex.getMessage(), ex);
        }
        if (glResult == null || !glResult.isSuccess()) {
            String message = glResult != null && glResult.getMessage() != null
                    ? glResult.getMessage()
                    : "General Ledger posting failed";
            throw new ApInvoiceApprovalException("Receipt posting failed: " + message);
        }

        receipt.setStatus(Receipt.ReceiptStatus.POSTED);
        return mapper.toDto(repository.save(receipt));
    }

    @Transactional
    public void deleteReceipt(UUID tenantId, UUID id) {
        Receipt receipt = getExistingReceipt(tenantId, id);
        if (receipt.getStatus() == Receipt.ReceiptStatus.POSTED) {
            throw new IllegalStateException("Posted receipts cannot be deleted");
        }
        repository.delete(receipt);
    }

    private void attachAllocations(Receipt receipt, UUID tenantId, List<ReceiptAllocationDto> dtos) {
        if (dtos == null) {
            return;
        }
        for (ReceiptAllocationDto dto : dtos) {
            if (dto.getSalesInvoiceId() == null || dto.getAllocatedAmount() == null) {
                continue;
            }
            ReceiptAllocation allocation = new ReceiptAllocation();
            allocation.setTenantId(tenantId);
            allocation.setReceipt(receipt);
            SalesInvoice invoice = salesInvoiceRepository
                    .findByTenantIdAndId(tenantId, dto.getSalesInvoiceId())
                    .orElseThrow(() -> new IllegalArgumentException("Sales invoice not found: " + dto.getSalesInvoiceId()));
            allocation.setSalesInvoice(invoice);
            allocation.setAllocatedAmount(dto.getAllocatedAmount());
            receipt.getAllocations().add(allocation);
        }
    }

    private List<ReceiptAllocationDto> toAllocationDtos(Receipt receipt) {
        if (receipt.getAllocations() == null) {
            return List.of();
        }
        return receipt.getAllocations().stream()
                .map(a -> {
                    ReceiptAllocationDto dto = new ReceiptAllocationDto();
                    dto.setSalesInvoiceId(a.getSalesInvoice().getId());
                    dto.setAllocatedAmount(a.getAllocatedAmount());
                    return dto;
                })
                .toList();
    }

    private Receipt getExistingReceipt(UUID tenantId, UUID id) {
        return repository
                .findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with id " + id));
    }
}
