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
@Table(name = "finance_refund")
public class Refund extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status = "PENDING_REVIEW";

    @Column(length = 1000)
    private String reason;
}

