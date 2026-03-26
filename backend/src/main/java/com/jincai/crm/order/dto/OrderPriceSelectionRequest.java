package com.jincai.crm.order.dto;

public record OrderPriceSelectionRequest(
    String travelerId,
    String departurePriceId,
    Integer quantity
) {
}
