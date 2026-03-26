package com.jincai.crm.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(
    String orderNo,
    @NotNull String customerId,
    @NotNull String routeId,
    @NotNull String departureId,
    @NotBlank String orderType,
    @NotBlank String productCategory,
    @Min(1) Integer travelerCount,
    BigDecimal totalAmount,
    String currency,
    List<@Valid OrderPriceSelectionRequest> priceSelections
) {
}
