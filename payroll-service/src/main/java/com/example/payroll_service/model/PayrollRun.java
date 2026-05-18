package com.example.payroll_service.model;

import com.example.payroll_service.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "payroll_runs")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PayrollRun extends Base {

    @Column(nullable = false)
    private LocalDate runDate;

    @Column(nullable = false)
    private UUID periodId;

    private BigDecimal totalGross;

    private BigDecimal totalNet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status;

    private UUID journalId;

    private LocalDate periodStartDate;

    private LocalDate periodEndDate;

    @OneToMany(mappedBy = "payrollRun")
    private Set<PayrollDetail> payrollDetails = new HashSet<>();
}
