package com.jincai.crm.finance.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RefundRequest(
    @NotNull String orderId,
    @NotNull BigDecimal amount,
    String reason
) {
}

