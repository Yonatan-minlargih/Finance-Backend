package com.finance.transactional.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class VendorDto {
    private UUID id;
    private UUID tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private String vendorCode;
    private String vendorName;
    private String taxId;
    private String contactEmail;
    private String contactPhone;
    private String paymentTerms;
    private Boolean isActive;
    private List<VendorAddressDto> addresses;
}
