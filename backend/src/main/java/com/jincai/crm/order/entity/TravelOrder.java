package com.jincai.crm.order.entity;

import com.jincai.crm.order.controller.*;
import com.jincai.crm.order.dto.*;
import com.jincai.crm.order.repository.*;
import com.jincai.crm.order.service.*;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "crm_order")
public class TravelOrder extends BaseEntity {

    @Column(name = "order_no", nullable = false, unique = true)
    private String orderNo;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @Column(name = "departure_id", nullable = false)
    private Long departureId;

    @Column(name = "order_type", nullable = false)
    private String orderType;

    @Column(name = "product_category", nullable = false)
    private String productCategory;

    @Column(name = "sales_user_id", nullable = false)
    private Long salesUserId;

    @Column(name = "sales_dept_id", nullable = false)
    private Long salesDeptId;

    @Column(name = "traveler_count", nullable = false)
    private Integer travelerCount;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false)
    private String currency = "CNY";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.DRAFT;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}

