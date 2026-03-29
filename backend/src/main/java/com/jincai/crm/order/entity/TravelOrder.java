package com.jincai.crm.order.entity;

import com.jincai.crm.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 旅行社订单实体。
 *
 * <p>代表一次完整的旅游产品预订和履约过程，是系统的核心业务实体。
 * 包含订单基本信息、状态管理、策略配置、时间跟踪等完整生命周期数据。
 *
 * <p><b>核心概念</b>：
 * <ul>
 *   <li><b>订单类型</b>：区分不同业务场景（如散客、团体、定制等）</li>
 *   <li><b>多维度状态</b>：通过主状态、合同状态、支付状态、库存状态、结算状态
 *       五个独立字段精确描述订单在不同业务维度的当前情况</li>
 *   <li><b>策略继承</b>：订单创建时从线路产品和出团日期继承默认策略，
 *       支持覆盖特定策略（如定金规则、支付时限等）</li>
 *   <li><b>时间跟踪</b>：完整记录订单从创建到完成的各个关键时间点</li>
 * </ul>
 *
 * @see OrderStatus 订单主状态枚举
 * @see ContractStatus 合同状态枚举
 * @see PaymentStatus 支付状态枚举
 * @see InventoryStatus 库存状态枚举
 * @see SettlementStatus 结算状态枚举
 */
@Getter
@Setter
@Entity
@Table(name = "crm_order")
public class TravelOrder extends BaseEntity {

    /**
     * 订单编号，全局唯一标识符。
     * 格式：DD + 时间戳（如 DD1648298400000），用于业务展示和查询。
     */
    @Column(name = "order_no", nullable = false, unique = true, length = 32)
    private String orderNo;

    /**
     * 客户ID，关联 {@code crm_customer} 表。
     * 订单归属的客户，用于客户关系管理和销售业绩统计。
     */
    @Column(name = "customer_id", nullable = false, length = 26)
    private String customerId;

    /**
     * 线路产品ID，关联 {@code product_route} 表。
     * 订单对应的旅游线路产品，包含行程、价格等基础信息。
     */
    @Column(name = "route_id", nullable = false, length = 26)
    private String routeId;

    /**
     * 出团日期ID，关联 {@code product_departure} 表。
     * 订单对应的具体出团班次，包含库存、价格等动态信息。
     */
    @Column(name = "departure_id", nullable = false, length = 26)
    private String departureId;

    /**
     * 订单类型，用于区分不同的业务场景。
     * 如：FIT（散客）、GROUP（团体）、CUSTOM（定制）等。
     */
    @Column(name = "order_type", nullable = false, length = 32)
    private String orderType;

    /**
     * 产品类别，用于产品分类统计和分析。
     * 如：DOMESTIC（国内游）、OVERSEAS（出境游）、CRUISE（邮轮）等。
     */
    @Column(name = "product_category", nullable = false, length = 32)
    private String productCategory;

    /**
     * 销售人员ID，关联 {@code org_user} 表。
     * 订单的销售人员，用于业绩归属和权限控制。
     */
    @Column(name = "sales_user_id", nullable = false, length = 26)
    private String salesUserId;

    /**
     * 销售部门ID，关联 {@code org_department} 表。
     * 销售人员所属部门，用于部门业绩统计和数据权限控制。
     */
    @Column(name = "sales_dept_id", nullable = false, length = 26)
    private String salesDeptId;

    /**
     * 出行人数，订单包含的旅客总数。
     * 用于库存扣减、价格计算、保险购买等业务逻辑。
     */
    @Column(name = "traveler_count", nullable = false)
    private Integer travelerCount;

    /**
     * 订单总金额，以 {@link #currency} 为单位。
     * 精度：18位数字，其中小数点后2位。
     */
    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    /**
     * 币种，采用 ISO 4217 标准三字母代码。
     * 系统当前仅支持人民币（CNY）。
     */
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "CNY";

    // ------------------------------------------------------------------ 状态管理

    /**
     * 订单主状态，描述订单在整个生命周期中的位置。
     *
     * <p>状态流转：
     * DRAFT（草稿）→ PENDING_APPROVAL（待审批）→ APPROVED（已批准）
     * → IN_TRAVEL（出行中）→ TRAVEL_FINISHED（出行结束）
     * → SETTLING（结算中）↔ COMPLETED（已完成）
     * ↘ REJECTED（已驳回）↗     ↗ CANCELED（已取消）↖
     *
     * @see OrderStatus 详细状态定义
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status = OrderStatus.DRAFT;

    /**
     * 提交时间，订单从草稿状态提交的时间戳。
     * 用于计算审批超时、自动取消等业务逻辑。
     */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /**
     * 审批通过时间，订单获得最终审批的时间戳。
     * 用于合同生效时间计算、库存锁定等业务逻辑。
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * 合同状态，描述订单合同的签署情况。
     *
     * @see ContractStatus 详细状态定义
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_status", nullable = false, length = 32)
    private ContractStatus contractStatus = ContractStatus.NOT_REQUIRED;

    /**
     * 支付状态，描述订单款项的收付情况。
     *
     * @see PaymentStatus 详细状态定义
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 32)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    /**
     * 库存状态，描述订单对出团日期库存的占用情况。
     *
     * @see InventoryStatus 详细状态定义
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_status", nullable = false, length = 32)
    private InventoryStatus inventoryStatus = InventoryStatus.UNLOCKED;

    /**
     * 结算状态，描述订单的财务结算完成情况。
     *
     * @see SettlementStatus 详细状态定义
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false, length = 32)
    private SettlementStatus settlementStatus = SettlementStatus.UNSETTLED;

    // ------------------------------------------------------------------ 策略配置

    /**
     * 是否需要签署合同。
     * 根据产品配置和客户类型决定，默认为 {@code false}。
     */
    @Column(name = "contract_required", nullable = false)
    private Boolean contractRequired = false;

    /**
     * 库存锁定策略，决定何时锁定出团日期库存。
     *
     * @see OrderLockPolicy 详细策略定义
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "lock_policy", nullable = false, length = 32)
    private OrderLockPolicy lockPolicy = OrderLockPolicy.ON_DEPOSIT;

    /**
     * 付款策略，决定订单的付款方式和阶段。
     *
     * @see OrderPaymentPolicy 详细策略定义
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_policy", nullable = false, length = 32)
    private OrderPaymentPolicy paymentPolicy = OrderPaymentPolicy.DEPOSIT_BALANCE;

    /**
     * 定金规则类型，决定定金是按比例还是固定金额计算。
     *
     * @see DepositRuleType 详细规则定义
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "deposit_type", nullable = false, length = 32)
    private DepositRuleType depositType = DepositRuleType.PERCENT;

    /**
     * 定金金额或比例。
     * 当 {@link #depositType} 为 {@code PERCENT} 时，表示百分比（如 30.00 表示 30%）；
     * 当为 {@code FIXED} 时，表示固定金额（单位：元）。
     */
    @Column(name = "deposit_value", precision = 18, scale = 2)
    private BigDecimal depositValue = new BigDecimal("30.00");

    /**
     * 定金支付截止天数，从订单创建时刻起计算的天数。
     * 用于提醒客户支付定金，超时可能触发自动取消。
     */
    @Column(name = "deposit_deadline_days")
    private Integer depositDeadlineDays = 3;

    /**
     * 尾款支付截止天数，从订单创建时刻起计算的天数。
     * 用于提醒客户支付尾款，通常在出行前一定天数截止。
     */
    @Column(name = "balance_deadline_days")
    private Integer balanceDeadlineDays = 7;

    /**
     * 自动取消超时小时数，从订单创建时刻起计算的小时数。
     * 超过此时间未支付定金的订单将被系统自动取消。
     */
    @Column(name = "auto_cancel_hours")
    private Integer autoCancelHours = 24;

    // ------------------------------------------------------------------ 时间跟踪

    /**
     * 合同签署时间，客户签署合同的时间戳。
     * 用于计算合同生效期、出行准备等业务逻辑。
     */
    @Column(name = "contract_signed_at")
    private LocalDateTime contractSignedAt;

    /**
     * 出行开始时间，订单对应旅游行程的实际开始时间。
     * 用于触发出行中状态变更、导游任务分配等业务逻辑。
     */
    @Column(name = "travel_started_at")
    private LocalDateTime travelStartedAt;

    /**
     * 出行结束时间，订单对应旅游行程的实际结束时间。
     * 用于触发出行结束状态变更、客户回访等业务逻辑。
     */
    @Column(name = "travel_finished_at")
    private LocalDateTime travelFinishedAt;

    /**
     * 订单完成时间，订单所有履约和结算工作完成的时间戳。
     * 用于业绩统计、数据分析等业务场景。
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 订单取消时间，订单被取消的时间戳。
     * 用于统计取消原因、计算退款金额等业务逻辑。
     */
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
}