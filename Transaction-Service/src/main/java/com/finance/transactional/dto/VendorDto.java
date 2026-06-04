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
    private String contactPerson;
    private String vatNumber;
    private String bankAccountNumber;
    private String defaultCurrency;
    private String defaultPaymentMethod;
    private String paymentTerms;
    private Boolean isActive;
    private String classification;
    private String paymentPriority;
    private Boolean emdWaiver;
    private List<VendorAddressDto> addresses;
}
