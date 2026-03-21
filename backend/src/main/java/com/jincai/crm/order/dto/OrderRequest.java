package com.jincai.crm.order.dto;

import com.jincai.crm.order.controller.*;
import com.jincai.crm.order.entity.*;
import com.jincai.crm.order.repository.*;
import com.jincai.crm.order.service.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(
    String orderNo,
    @NotNull Long customerId,
    @NotNull Long routeId,
    @NotNull Long departureId,
    @NotBlank String orderType,
    @NotBlank String productCategory,
    @Min(1) Integer travelerCount,
    BigDecimal totalAmount,
    String currency,
    List<@Valid OrderPriceSelectionRequest> priceSelections
) {
}
