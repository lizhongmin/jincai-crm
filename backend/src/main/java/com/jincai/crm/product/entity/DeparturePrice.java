package com.jincai.crm.product.entity;

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
@Table(name = "product_departure_price")
public class DeparturePrice extends BaseEntity {

    @Column(name = "departure_id", nullable = false)
    private String departureId;

    @Column(name = "price_type", nullable = false)
    private String priceType;

    @Column(name = "price_label")
    private String priceLabel;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String currency = "CNY";

    @Column(length = 1000)
    private String description;
}
