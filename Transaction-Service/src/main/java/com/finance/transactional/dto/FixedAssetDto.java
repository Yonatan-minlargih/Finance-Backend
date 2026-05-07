package com.finance.transactional.dto;

import com.finance.transactional.model.asset.FixedAsset;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class FixedAssetDto {
    private UUID id;
    private UUID tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private String assetCode;
    private String assetName;
    private String assetCategory;
    private LocalDate acquisitionDate;
    private BigDecimal acquisitionCost;
    private BigDecimal salvageValue;
    private Integer usefulLifeYears;
    private String depreciationMethod;
    private BigDecimal accumulatedDepreciation;
    private BigDecimal netBookValue;
    private FixedAsset.AssetStatus status;
}
