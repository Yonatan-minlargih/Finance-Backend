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
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "profitability_analysis")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfitabilityAnalysis extends Base {

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal revenue;

    @Column(name = "cogs", nullable = false, precision = 19, scale = 4)
    private BigDecimal cogs;

    @Column(name = "gross_profit", nullable = false, precision = 19, scale = 4)
    private BigDecimal grossProfit;

    @Column(name = "analysis_date")
    private LocalDate analysisDate;

    @Column(name = "period_id")
    private UUID periodId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "cost_center_id")
    private CostCenter costCenter;

    @ManyToOne
    @JoinColumn(name = "profit_center_id")
    private ProfitCenter profitCenter;
}
