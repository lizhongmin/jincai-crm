package com.jincai.crm.finance.entity;

import com.jincai.crm.finance.controller.*;
import com.jincai.crm.finance.dto.*;
import com.jincai.crm.finance.repository.*;
import com.jincai.crm.finance.service.*;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "finance_receivable")
public class Receivable extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal received = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status = "OPEN";
}

