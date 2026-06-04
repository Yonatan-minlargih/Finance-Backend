package com.financial.corefinance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrialBalanceReportResponse {
    private LocalDate asOfDate;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private boolean balanced;
    private TrialBalanceLineResponse apPayableLine;
    private TrialBalanceLineResponse arReceivableLine;
    private List<TrialBalanceLineResponse> lines;
}
