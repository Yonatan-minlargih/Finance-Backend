package com.finance.transactional.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class ReceiptAllocationDto {
    private UUID id;
    private UUID tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private UUID receiptId;
    private UUID salesInvoiceId;
    private BigDecimal allocatedAmount;
}
