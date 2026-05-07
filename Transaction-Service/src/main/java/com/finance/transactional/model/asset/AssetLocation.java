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

@Entity
@Table(name = "asset_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetLocation extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private FixedAsset asset;

    @Column(name = "location_name", length = 100, nullable = false)
    private String locationName;

    @Column(name = "department_code", length = 50)
    private String departmentCode; // For tracking which department uses cost center

    @Column(name = "custodian_name", length = 100)
    private String custodianName;

    @Column(name = "is_current")
    private Boolean isCurrent = true;
}
