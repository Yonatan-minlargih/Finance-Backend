package com.finance.transactional.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArAgingReportDto {
    private LocalDate asOfDate;
    private List<Integer> bucketBoundaries;
    private Map<String, BigDecimal> buckets;
    private BigDecimal totalOutstanding;
    private List<ArAgingReportLineDto> lines;
}
