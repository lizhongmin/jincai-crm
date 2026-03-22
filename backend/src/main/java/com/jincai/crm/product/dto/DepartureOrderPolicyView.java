package com.jincai.crm.product.dto;

public record DepartureOrderPolicyView(
    OrderPolicyView effective,
    OrderPolicyRequest overrides
) {
}

