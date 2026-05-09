package com.financial.corefinance.domain.entity;

import com.financial.corefinance.domain.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "disclosure_templates", indexes = {
    @Index(name = "idx_disclosure_templates_tenant", columnList = "tenant_id")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uq_disclosure_templates_tenant_code", columnNames = {"tenant_id", "template_code"})
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DisclosureTemplate extends BaseEntity {

    @NotBlank(message = "Template code is required")
    @Column(name = "template_code", length = 50, nullable = false)
    private String templateCode;

    @NotBlank(message = "Template name is required")
    @Column(name = "template_name", length = 200, nullable = false)
    private String templateName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "ifrs_standard", length = 50)
    private String ifrsStandard; // IAS_1, IAS_12, IFRS_16, etc.

    @Column(name = "disclosure_category", length = 100)
    private String disclosureCategory;

    @Column(name = "template_type", length = 20)
    @Builder.Default
    private String templateType = "TEXT"; // TEXT, TABLE, CALCULATION

    @Column(name = "template_content", columnDefinition = "TEXT")
    private String templateContent;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_mandatory")
    @Builder.Default
    private Boolean isMandatory = false;

    @Column(name = "frequency", length = 20)
    @Builder.Default
    private String frequency = "ANNUAL"; // ANNUAL, QUARTERLY

    @Column(name = "template_version")
    @Builder.Default
    private String templateVersion = "1.0";

    @Column(name = "effective_from")
    private java.time.LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private java.time.LocalDate effectiveTo;

    @Builder.Default
    @OneToMany(mappedBy = "disclosureTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DisclosureItem> disclosureItems = new ArrayList<>();
}
