package com.jincai.crm.order.service;

import com.jincai.crm.order.entity.OrderPriceItem;
import com.jincai.crm.order.entity.OrderTravelerSnapshot;

import java.math.BigDecimal;
import java.util.List;

/**
 * 价格计算结果。
 *
 * <p>封装订单价格计算的所有结果信息，包括旅客快照、价格项、总价等。
 * 用于在价格计算服务和订单服务之间传递计算结果。
 *
 * @param routeId 线路ID
 * @param departureId 出团日期ID
 * @param travelerSnapshots 旅客快照列表
 * @param priceItems 价格项列表
 * @param totalAmount 总金额
 * @param currency 币种
 */
public record PricingCalculation(
    String routeId,
    String departureId,
    List<OrderTravelerSnapshot> travelerSnapshots,
    List<OrderPriceItem> priceItems,
    BigDecimal totalAmount,
    String currency
) {
}