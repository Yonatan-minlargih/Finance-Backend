package com.financial.corefinance.dto.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApPaymentPostedEvent {
    private UUID id;
    private UUID tenantId;
    private String paymentNumber;
    private LocalDate paymentDate;
    private BigDecimal amount;
}
