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

@Entity
@Table(name = "numbering_series", indexes = {
        @Index(name = "idx_numbering_series_tenant", columnList = "tenant_id")
},
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_numbering_series_tenant_code", columnNames = {"tenant_id", "series_code"})
        })
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NumberingSeries extends BaseEntity {

    @NotBlank(message = "Series code is required")
    @Column(name = "series_code", length = 50, nullable = false)
    private String seriesCode;

    @NotBlank(message = "Series name is required")
    @Column(name = "series_name", length = 100, nullable = false)
    private String seriesName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "prefix", length = 20)
    private String prefix;

    @Column(name = "suffix", length = 20)
    private String suffix;

    @NotNull(message = "Current number is required")
    @Column(name = "current_number", nullable = false)
    @Builder.Default
    private Long currentNumber = 1L;

    @Column(name = "start_number")
    @Builder.Default
    private Long startNumber = 1L;

    @Column(name = "end_number")
    private Long endNumber;

    @Column(name = "number_length")
    @Builder.Default
    private Integer numberLength = 6;

    @Column(name = "reset_frequency", length = 20)
    private String resetFrequency; // NEVER, YEARLY, MONTHLY, DAILY

    @Column(name = "last_reset_date")
    private java.time.LocalDate lastResetDate;

    @Column(name = "number_separator", length = 5)
    @Builder.Default
    private String separator = "-";

    @Column(name = "format", length = 100)
    private String format; // e.g., "PREFIX{YYYY}{MM}{NUMBER:6}SUFFIX"

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "allow_manual_override")
    @Builder.Default
    private Boolean allowManualOverride = false;

    @PrePersist
    @PreUpdate
    public void generateFormat() {
        if (format == null || format.isEmpty()) {
            StringBuilder formatBuilder = new StringBuilder();
            if (prefix != null && !prefix.isEmpty()) {
                formatBuilder.append(prefix);
                formatBuilder.append(separator);
            }
            formatBuilder.append("{NUMBER:").append(numberLength).append("}");
            if (suffix != null && !suffix.isEmpty()) {
                formatBuilder.append(separator);
                formatBuilder.append(suffix);
            }
            this.format = formatBuilder.toString();
        }
    }
    public String generateNextNumber() {
        if (format != null && !format.isEmpty()) {
            String formatted = format.replace("{NUMBER:" + numberLength + "}",
                    String.format("%0" + numberLength + "d", currentNumber));
            formatted = formatted.replace("{YYYY}", String.valueOf(java.time.Year.now()));
            formatted = formatted.replace("{MM}", String.format("%02d", java.time.LocalDate.now().getMonthValue()));
            formatted = formatted.replace("{DD}", String.format("%02d", java.time.LocalDate.now().getDayOfMonth()));
            return formatted;
        }

        StringBuilder numberBuilder = new StringBuilder();

        if (prefix != null && !prefix.isEmpty()) {
            numberBuilder.append(prefix);
            numberBuilder.append(separator);
        }

        numberBuilder.append(String.format("%0" + numberLength + "d", currentNumber));

        if (suffix != null && !suffix.isEmpty()) {
            numberBuilder.append(separator);
            numberBuilder.append(suffix);
        }

        return numberBuilder.toString();
    }

    public void incrementNumber() {
        this.currentNumber++;
    }
}