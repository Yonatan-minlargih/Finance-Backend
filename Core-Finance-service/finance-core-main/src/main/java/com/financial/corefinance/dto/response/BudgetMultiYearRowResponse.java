package com.financial.corefinance.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BudgetMultiYearRowResponse {
    private UUID departmentId;
    private UUID accountId;
    private String rowLabel;
    private List<BudgetMultiYearColumnResponse> years;
}
