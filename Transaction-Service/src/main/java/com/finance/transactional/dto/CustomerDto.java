package com.finance.transactional.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class CustomerDto {
    private UUID id;
    private UUID tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private String customerCode;
    private String customerName;
    private String taxId;
    private String contactEmail;
    private String contactPhone;
    private Double creditLimit;
    private String paymentTerms;
    private Boolean isActive;
}
