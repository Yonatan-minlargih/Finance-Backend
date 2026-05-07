package com.finance.transactional.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class AssetTransactionDto {
    private UUID id;
    private UUID tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private UUID assetId;
    private String transactionType;
    private LocalDate transactionDate;
    private BigDecimal amount;
    private String description;
}
