package com.jincai.crm.finance.dto;

import java.math.BigDecimal;

public record FinanceOrderOptionView(
    Long id,
    String orderNo,
    String status,
    Long customerId,
    BigDecimal totalAmount,
    String currency
) {
}
