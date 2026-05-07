package com.finance.transactional.model.asset;

import com.finance.transactional.model.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fixed_assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FixedAsset extends BaseTenantEntity {

    @Column(name = "asset_code", length = 50, nullable = false)
    private String assetCode;

    @Column(name = "asset_name", length = 255, nullable = false)
    private String assetName;

    @Column(name = "asset_category", length = 100)
    private String assetCategory;

    @Column(name = "acquisition_date", nullable = false)
    private LocalDate acquisitionDate;

    @Column(name = "acquisition_cost", precision = 15, scale = 2)
    private BigDecimal acquisitionCost;

    @Column(name = "salvage_value", precision = 15, scale = 2)
    private BigDecimal salvageValue;

    @Column(name = "useful_life_years")
    private Integer usefulLifeYears;

    @Column(name = "depreciation_method", length = 50)
    private String depreciationMethod; // e.g. STRAIGHT_LINE, DECLINING_BALANCE

    @Column(name = "accumulated_depreciation", precision = 15, scale = 2)
    private BigDecimal accumulatedDepreciation = BigDecimal.ZERO;

    @Column(name = "net_book_value", precision = 15, scale = 2)
    private BigDecimal netBookValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private AssetStatus status;

    public enum AssetStatus {
        ACTIVE, DISPOSED, IMPAIRED, IN_MAINTENANCE
    }
}
