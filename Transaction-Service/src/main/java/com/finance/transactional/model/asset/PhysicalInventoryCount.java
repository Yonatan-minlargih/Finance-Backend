package com.finance.transactional.model.asset;

import com.finance.transactional.model.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "physical_inventory_counts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalInventoryCount extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private FixedAsset asset;

    @Column(name = "count_date", nullable = false)
    private LocalDate countDate;

    @Column(name = "counted_by", length = 100)
    private String countedBy;

    @Column(name = "is_found")
    private Boolean isFound;

    @Column(name = "condition_notes", length = 255)
    private String conditionNotes;
}
