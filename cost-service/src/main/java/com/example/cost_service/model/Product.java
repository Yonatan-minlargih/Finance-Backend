package com.example.cost_service.model;

import com.example.cost_service.enums.ActiveStatus;
import com.example.cost_service.enums.ProductCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product extends Base {

    @Column(unique = true)
    private String productCode;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    private ActiveStatus isActive;

    @OneToMany(mappedBy = "product")
    private Set<CostRecord> costRecords = new HashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<StandardCostRate> standardCostRates = new HashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<ProfitabilityAnalysis> profitabilityAnalyses = new HashSet<>();
}
