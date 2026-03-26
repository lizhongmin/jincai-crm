package com.jincai.crm.order.entity;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "crm_order")
public class TravelOrder extends BaseEntity {

    @Column(name = "order_no", nullable = false, unique = true)
    private String orderNo;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "route_id", nullable = false)
    private String routeId;

    @Column(name = "departure_id", nullable = false)
    private String departureId;

    @Column(name = "order_type", nullable = false)
    private String orderType;

    @Column(name = "product_category", nullable = false)
    private String productCategory;

    @Column(name = "sales_user_id", nullable = false)
    private String salesUserId;

    @Column(name = "sales_dept_id", nullable = false)
    private String salesDeptId;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_status", nullable = false)
    private ContractStatus contractStatus = ContractStatus.NOT_REQUIRED;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_status", nullable = false)
    private InventoryStatus inventoryStatus = InventoryStatus.UNLOCKED;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false)
    private SettlementStatus settlementStatus = SettlementStatus.UNSETTLED;

    @Column(name = "contract_required", nullable = false)
    private Boolean contractRequired = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "lock_policy", nullable = false)
    private OrderLockPolicy lockPolicy = OrderLockPolicy.ON_DEPOSIT;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_policy", nullable = false)
    private OrderPaymentPolicy paymentPolicy = OrderPaymentPolicy.DEPOSIT_BALANCE;

    @Enumerated(EnumType.STRING)
    @Column(name = "deposit_type", nullable = false)
    private DepositRuleType depositType = DepositRuleType.PERCENT;

    @Column(name = "deposit_value", precision = 18, scale = 2)
    private BigDecimal depositValue = new BigDecimal("30.00");

    @Column(name = "deposit_deadline_days")
    private Integer depositDeadlineDays = 3;

    @Column(name = "balance_deadline_days")
    private Integer balanceDeadlineDays = 7;

    @Column(name = "auto_cancel_hours")
    private Integer autoCancelHours = 24;

    @Column(name = "contract_signed_at")
    private LocalDateTime contractSignedAt;

    @Column(name = "travel_started_at")
    private LocalDateTime travelStartedAt;

    @Column(name = "travel_finished_at")
    private LocalDateTime travelFinishedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
}
