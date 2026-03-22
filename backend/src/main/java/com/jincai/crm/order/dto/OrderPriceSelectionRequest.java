package com.jincai.crm.order.dto;

public record OrderPriceSelectionRequest(
    Long travelerId,
    Long departurePriceId,
    Integer quantity
) {
}
