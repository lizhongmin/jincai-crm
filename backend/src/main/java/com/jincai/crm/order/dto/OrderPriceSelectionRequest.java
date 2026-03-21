package com.jincai.crm.order.dto;

import com.jincai.crm.order.controller.*;
import com.jincai.crm.order.entity.*;
import com.jincai.crm.order.repository.*;
import com.jincai.crm.order.service.*;

import jakarta.validation.constraints.NotNull;

public record OrderPriceSelectionRequest(
    Long travelerId,
    @NotNull Long departurePriceId,
    Integer quantity
) {
}
