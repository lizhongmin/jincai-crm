package com.jincai.crm.order.dto;

import com.jincai.crm.order.entity.OrderPriceItem;
import com.jincai.crm.order.entity.OrderTravelerSnapshot;

import java.math.BigDecimal;
import java.util.List;

public record OrderQuoteView(
    String routeId,
    String departureId,
    Integer travelerCount,
    BigDecimal totalAmount,
    String currency,
    List<OrderTravelerSnapshot> travelers,
    List<OrderPriceItem> priceItems
) {
}
