package com.jincai.crm.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DeparturePriceRequest(
    @NotBlank String priceType,
    String priceLabel,
    @NotNull BigDecimal price,
    String currency,
    String description
) {
}
