package com.jincai.crm.product.dto;

import com.jincai.crm.product.controller.*;
import com.jincai.crm.product.entity.*;
import com.jincai.crm.product.repository.*;
import com.jincai.crm.product.service.*;

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
