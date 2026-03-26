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
@Table(name = "finance_payment")
public class Payment extends BaseEntity {

    @Column(name = "payable_id", nullable = false)
    private String payableId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status = "PENDING_REVIEW";

    @Column(length = 1000)
    private String remark;
}

