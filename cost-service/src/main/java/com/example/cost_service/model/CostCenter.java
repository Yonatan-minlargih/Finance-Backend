package com.example.cost_service.model;

import com.example.cost_service.enums.ActiveStatus;
import com.example.cost_service.enums.CostCenterType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "cost_centers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CostCenter extends Base {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private CostCenterType type;

    @Enumerated(EnumType.STRING)
    private ActiveStatus isActive;

    @ManyToOne
    @JoinColumn(name = "profit_center_id")
    private ProfitCenter profitCenter;

    @OneToMany(mappedBy = "costCenter")
    private Set<CostRecord> costRecords = new HashSet<>();

    @OneToMany(mappedBy = "costCenter")
    private Set<ProfitabilityAnalysis> profitabilityAnalyses = new HashSet<>();
}
