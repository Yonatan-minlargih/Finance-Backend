package com.finance.transactional.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InvoiceVoidRequest {

    @NotBlank(message = "Reversal reason is required")
    private String reversalReason;
}
