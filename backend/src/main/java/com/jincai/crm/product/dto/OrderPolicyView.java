package com.jincai.crm.product.dto;

import com.jincai.crm.order.entity.DepositRuleType;
import com.jincai.crm.order.entity.OrderLockPolicy;
import com.jincai.crm.order.entity.OrderPaymentPolicy;

import java.math.BigDecimal;

public record OrderPolicyView(
    Boolean contractRequired,
    OrderLockPolicy lockPolicy,
    OrderPaymentPolicy paymentPolicy,
    DepositRuleType depositType,
    BigDecimal depositValue,
    Integer depositDeadlineDays,
    Integer balanceDeadlineDays,
    Integer autoCancelHours
) {
}

