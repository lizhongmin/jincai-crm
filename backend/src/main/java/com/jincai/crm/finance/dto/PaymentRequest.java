package com.jincai.crm.finance.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(
    @NotNull String payableId,
    @NotNull BigDecimal amount,
    String remark
) {
}

