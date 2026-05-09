package com.financial.corefinance.domain.entity;

import com.financial.corefinance.domain.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "disclosure_items", indexes = {
    @Index(name = "idx_disclosure_items_tenant", columnList = "tenant_id"),
    @Index(name = "idx_disclosure_items_template", columnList = "disclosure_template_id"),
    @Index(name = "idx_disclosure_items_fiscal_year", columnList = "fiscal_year_id")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DisclosureItem extends BaseEntity {

    @NotNull(message = "Disclosure template is required")
    @Column(name = "disclosure_template_id", nullable = false)
    private UUID disclosureTemplateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disclosure_template_id", insertable = false, updatable = false)
    private DisclosureTemplate disclosureTemplate;

    @NotNull(message = "Fiscal year is required")
    @Column(name = "fiscal_year_id", nullable = false)
    private UUID fiscalYearId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_year_id", insertable = false, updatable = false)
    private FiscalYear fiscalYear;

    @Column(name = "item_number", length = 20)
    private String itemNumber;

    @NotBlank(message = "Item title is required")
    @Column(name = "item_title", length = 500, nullable = false)
    private String itemTitle;

    @Column(name = "item_content", columnDefinition = "TEXT")
    private String itemContent;

    @Column(name = "item_type", length = 20)
    @Builder.Default
    private String itemType = "TEXT"; // TEXT, TABLE, AMOUNT, PERCENTAGE

    @Column(name = "amount_value", precision = 19, scale = 4)
    private BigDecimal amountValue;

    @Column(name = "percentage_value", precision = 5, scale = 2)
    private BigDecimal percentageValue;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "as_of_date")
    private LocalDate asOfDate;

    @Column(name = "comparative_amount", precision = 19, scale = 4)
    private BigDecimal comparativeAmount;

    @Column(name = "comparative_date")
    private LocalDate comparativeDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DisclosureStatus status = DisclosureStatus.DRAFT;

    @Column(name = "reviewed_at")
    private LocalDate reviewedAt;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "approved_at")
    private LocalDate approvedAt;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "source_reference", length = 500)
    private String sourceReference;

    public enum DisclosureStatus {
        DRAFT,
        UNDER_REVIEW,
        REVIEWED,
        APPROVED,
        PUBLISHED,
        ARCHIVED
    }
}
