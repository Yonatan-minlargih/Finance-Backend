package com.finance.transactional.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class ReceiptDto {
    private UUID id;
    private UUID tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private String receiptNumber;
    private UUID customerId;
    private UUID bankAccountId;
    private LocalDate receiptDate;
    private BigDecimal amount;
    private String paymentMethod;
    private String referenceNumber;
    private List<ReceiptAllocationDto> allocations;
}
