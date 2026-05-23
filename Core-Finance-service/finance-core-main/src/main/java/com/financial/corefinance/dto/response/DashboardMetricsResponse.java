package com.financial.corefinance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsResponse {
    private BigDecimal totalBalance;
    private long activeAccounts;
    private BigDecimal monthlyGrowth;
    private long totalReports;
}
