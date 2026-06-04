package com.financial.corefinance.dto.response;

import com.financial.corefinance.domain.entity.JournalHeader;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class YearEndCloseResult {
    JournalHeader closingJournal;
    int nominalAccountsClosed;
    BigDecimal netIncome;
    boolean fiscalYearMarkedClosed;
}
