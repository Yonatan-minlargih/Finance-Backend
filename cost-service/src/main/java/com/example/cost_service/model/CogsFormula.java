package com.example.cost_service.model;

import com.example.cost_service.enums.FormulaType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "cogs_formulas")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CogsFormula extends Base {

    @Enumerated(EnumType.STRING)
    @Column(name = "formula_name")
    private FormulaType formulaName;

    @Column(name = "formula_json", columnDefinition = "TEXT")
    private String formulaJson;

    @Column(name = "period_id")
    private UUID periodId;
}
