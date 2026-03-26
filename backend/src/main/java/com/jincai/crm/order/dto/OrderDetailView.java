package com.jincai.crm.order.dto;

import com.jincai.crm.order.entity.OrderPriceItem;
import com.jincai.crm.order.entity.OrderTravelerSnapshot;
import com.jincai.crm.order.entity.TravelOrder;

import java.util.List;

public record OrderDetailView(
    TravelOrder order,
    List<OrderTravelerSnapshot> travelers,
    List<OrderPriceItem> priceItems
) {
}
