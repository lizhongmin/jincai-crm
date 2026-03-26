package com.jincai.crm.finance.entity;

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
@Table(name = "finance_payable")
public class Payable extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal paid = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status = "OPEN";
}

