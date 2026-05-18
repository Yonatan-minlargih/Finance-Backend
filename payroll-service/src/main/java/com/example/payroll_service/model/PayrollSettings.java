package com.example.payroll_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "payroll_settings")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PayrollSettings extends Base {

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal pensionRate = new BigDecimal("0.0700");

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal overtimeNormalRate = new BigDecimal("1.25");

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal overtimeNightRate = new BigDecimal("1.50");

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal overtimeHolidayRate = new BigDecimal("2.00");

    @Column(nullable = false, columnDefinition = "TEXT")
    private String taxBandsJson = "[" +
            "{\"limit\": 600, \"rate\": 0.00, \"deduction\": 0}," +
            "{\"limit\": 1650, \"rate\": 0.10, \"deduction\": 60}," +
            "{\"limit\": 3200, \"rate\": 0.15, \"deduction\": 142.5}," +
            "{\"limit\": 5250, \"rate\": 0.20, \"deduction\": 302.5}," +
            "{\"limit\": 7800, \"rate\": 0.25, \"deduction\": 565}," +
            "{\"limit\": 10900, \"rate\": 0.30, \"deduction\": 955}," +
            "{\"limit\": 9999999, \"rate\": 0.35, \"deduction\": 1500}" +
            "]";
}
