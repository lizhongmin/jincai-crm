package com.jincai.crm.product.entity;

import com.jincai.crm.common.BaseEntity;
import com.jincai.crm.order.entity.DepositRuleType;
import com.jincai.crm.order.entity.OrderLockPolicy;
import com.jincai.crm.order.entity.OrderPaymentPolicy;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "product_departure")
public class Departure extends BaseEntity {

    @Column(name = "route_id", nullable = false)
    private String routeId;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "registration_deadline")
    private LocalDate registrationDeadline;

    @Column(name = "min_group_size")
    private Integer minGroupSize;

    @Column(name = "max_group_size")
    private Integer maxGroupSize;

    @Column(nullable = false)
    private String status = "OPEN";

    @Column(name = "gathering_place")
    private String gatheringPlace;

    @Column(name = "departure_notice", length = 2000)
    private String departureNotice;

    @Column(name = "contract_required_override")
    private Boolean contractRequiredOverride;

    @Enumerated(EnumType.STRING)
    @Column(name = "lock_policy_override")
    private OrderLockPolicy lockPolicyOverride;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_policy_override")
    private OrderPaymentPolicy paymentPolicyOverride;

    @Enumerated(EnumType.STRING)
    @Column(name = "deposit_type_override")
    private DepositRuleType depositTypeOverride;

    @Column(name = "deposit_value_override", precision = 18, scale = 2)
    private BigDecimal depositValueOverride;

    @Column(name = "deposit_deadline_days_override")
    private Integer depositDeadlineDaysOverride;

    @Column(name = "balance_deadline_days_override")
    private Integer balanceDeadlineDaysOverride;

    @Column(name = "auto_cancel_hours_override")
    private Integer autoCancelHoursOverride;
}
