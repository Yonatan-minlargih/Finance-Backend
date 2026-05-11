package com.example.cost_service.model;

import com.example.cost_service.enums.ApplicableTo;
import com.example.cost_service.enums.TaxType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "withholding_tax_rules")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithholdingTaxRule extends Base {

    @Column(name = "tax_name")
    private String taxName;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type")
    private TaxType taxType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal rate;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicable_to")
    private ApplicableTo applicableTo;
}
