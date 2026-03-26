package com.jincai.crm.finance.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ReceiptRequest(
    @NotNull String receivableId,
    @NotNull BigDecimal amount,
    String remark
) {
}

