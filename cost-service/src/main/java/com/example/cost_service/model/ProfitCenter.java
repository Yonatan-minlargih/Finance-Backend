package com.example.cost_service.model;

import com.example.cost_service.enums.ActiveStatus;
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
@Table(name = "profit_centers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfitCenter extends Base {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private ActiveStatus isActive;

    @OneToMany(mappedBy = "profitCenter")
    private Set<CostCenter> costCenters = new HashSet<>();

    @OneToMany(mappedBy = "profitCenter")
    private Set<ProfitabilityAnalysis> profitabilityAnalyses = new HashSet<>();
}
