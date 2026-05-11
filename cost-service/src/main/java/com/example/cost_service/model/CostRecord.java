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
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "cost_records")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CostRecord extends Base {

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal materialCost;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal laborCost;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal overheadCost;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalCost;

    @Column(name = "period_id")
    private UUID periodId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "cost_center_id")
    private CostCenter costCenter;
}
