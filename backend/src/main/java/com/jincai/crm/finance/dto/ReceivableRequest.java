package com.jincai.crm.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ReceivableRequest(
    @NotBlank String itemName,
    @NotNull BigDecimal amount
) {
}

