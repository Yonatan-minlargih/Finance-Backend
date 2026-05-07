package com.finance.transactional.model.ar;

import com.finance.transactional.model.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends BaseTenantEntity {

    @Column(name = "customer_code", length = 50, nullable = false)
    private String customerCode;

    @Column(name = "customer_name", length = 255, nullable = false)
    private String customerName;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "credit_limit")
    private Double creditLimit;

    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
