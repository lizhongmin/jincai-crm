package com.jincai.crm.order.entity;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "crm_order_price_item")
public class OrderPriceItem extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "traveler_id")
    private String travelerId;

    @Column(name = "traveler_name")
    private String travelerName;

    @Column(name = "departure_price_id", nullable = false)
    private String departurePriceId;

    @Column(name = "price_type", nullable = false)
    private String priceType;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency = "CNY";
}
