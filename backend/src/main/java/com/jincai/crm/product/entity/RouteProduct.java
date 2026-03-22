package com.jincai.crm.product.entity;

import com.jincai.crm.product.controller.*;
import com.jincai.crm.product.dto.*;
import com.jincai.crm.product.repository.*;
import com.jincai.crm.product.service.*;

import com.jincai.crm.common.BaseEntity;
import com.jincai.crm.order.entity.DepositRuleType;
import com.jincai.crm.order.entity.OrderLockPolicy;
import com.jincai.crm.order.entity.OrderPaymentPolicy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_route")
public class RouteProduct extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(name = "departure_city")
    private String departureCity;

    @Column(name = "destination_city")
    private String destinationCity;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "duration_nights")
    private Integer durationNights;

    private String transportation;

    @Column(name = "hotel_standard")
    private String hotelStandard;

    @Column(name = "highlights", length = 2000)
    private String highlights;

    @Column(name = "fee_includes", length = 2000)
    private String feeIncludes;

    @Column(name = "fee_excludes", length = 2000)
    private String feeExcludes;

    @Column(name = "booking_notice", length = 2000)
    private String bookingNotice;

    @Column(length = 2000)
    private String description;

    @Column(name = "contract_required_default", nullable = false)
    private Boolean contractRequiredDefault = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "lock_policy_default", nullable = false)
    private OrderLockPolicy lockPolicyDefault = OrderLockPolicy.ON_DEPOSIT;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_policy_default", nullable = false)
    private OrderPaymentPolicy paymentPolicyDefault = OrderPaymentPolicy.DEPOSIT_BALANCE;

    @Enumerated(EnumType.STRING)
    @Column(name = "deposit_type_default", nullable = false)
    private DepositRuleType depositTypeDefault = DepositRuleType.PERCENT;

    @Column(name = "deposit_value_default", nullable = false, precision = 18, scale = 2)
    private BigDecimal depositValueDefault = new BigDecimal("30.00");

    @Column(name = "deposit_deadline_days_default", nullable = false)
    private Integer depositDeadlineDaysDefault = 3;

    @Column(name = "balance_deadline_days_default", nullable = false)
    private Integer balanceDeadlineDaysDefault = 7;

    @Column(name = "auto_cancel_hours_default", nullable = false)
    private Integer autoCancelHoursDefault = 24;
}
