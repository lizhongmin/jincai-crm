package com.jincai.crm.finance.dto;

import java.math.BigDecimal;

public record FinanceOrderOptionView(
    String id,
    String orderNo,
    String status,
    String customerId,
    BigDecimal totalAmount,
    String currency
) {
}
