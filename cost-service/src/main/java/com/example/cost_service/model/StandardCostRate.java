package com.example.cost_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "standard_cost_rates")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardCostRate extends Base {

    @Column(name = "item_code")
    private String itemCode;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal rate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
