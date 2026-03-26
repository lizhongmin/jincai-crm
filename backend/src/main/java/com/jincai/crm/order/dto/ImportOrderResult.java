package com.jincai.crm.order.dto;

import java.util.List;

public record ImportOrderResult(
    int success,
    int failed,
    List<String> errors
) {
}

