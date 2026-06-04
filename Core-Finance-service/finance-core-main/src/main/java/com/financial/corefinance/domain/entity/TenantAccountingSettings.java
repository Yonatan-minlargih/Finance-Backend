package com.financial.corefinance.domain.entity;

import com.financial.corefinance.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tenant_accounting_settings",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_tenant_accounting_settings_tenant", columnNames = {"tenant_id"})
    })
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TenantAccountingSettings extends BaseEntity {

    @Column(name = "fiscal_year_start_month")
    @Builder.Default
    private Integer fiscalYearStartMonth = 1; // 1=January

    @Column(name = "retained_earnings_account_code", length = 50)
    private String retainedEarningsAccountCode;

    @Column(name = "ifrs_compliance")
    @Builder.Default
    private Boolean ifrsCompliance = true;

    @Column(name = "gaap_adaptation")
    @Builder.Default
    private Boolean gaapAdaptation = false;

    @Column(name = "local_audit")
    @Builder.Default
    private Boolean localAudit = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_closing_type", length = 10)
    @Builder.Default
    private PeriodClosingType periodClosingType = PeriodClosingType.SOFT;

    @Column(name = "base_currency", length = 3)
    @Builder.Default
    private String baseCurrency = "ETB";

    public enum PeriodClosingType {
        SOFT, HARD
    }
}
