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

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "depreciation_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepreciationSchedule extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private FixedAsset asset;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "depreciation_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal depreciationAmount;

    @Column(name = "is_posted")
    private Boolean isPosted = false;
}
