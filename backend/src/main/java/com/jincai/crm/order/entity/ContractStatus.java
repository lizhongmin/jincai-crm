package com.jincai.crm.order.entity;

/**
 * 订单合同状态枚举。
 *
 * <p>描述旅游订单合同的签署和执行情况，与主状态（OrderStatus）相互关联，
 * 共同决定订单是否可以进入出行阶段。
 *
 * <p><b>状态流转</b>：
 * <pre>
 * NOT_REQUIRED（无需合同）
 *        │
 *        ▼
 *   PENDING_SIGN（待签署）
 *        │
 *        ▼
 *     SIGNED（已签署）
 * </pre>
 *
 * <p><b>业务规则</b>：
 * <ul>
 *   <li>订单状态为 {@code APPROVED} 时，如 {@code contractRequired == true}，
 *       则 {@code contractStatus} 必须为 {@code SIGNED} 才能确认出行</li>
 *   <li>订单创建时根据产品配置自动设置初始状态</li>
 *   <li>合同签署后，状态从 {@code PENDING_SIGN} 转为 {@code SIGNED}</li>
 *   <li>合同一旦签署不可撤销（除非订单取消）</li>
 * </ul>
 *
 * @see TravelOrder#contractStatus 订单合同状态字段
 * @see TravelOrder#contractRequired 是否需要合同
 * @see OrderStatus 订单主状态
 */
public enum ContractStatus {
    /**
     * 无需合同。
     *
     * <p>表示该订单无需签署任何形式的合同，可能的场景包括：
     * <ul>
     *   <li>产品配置为无需合同（如部分短途游）</li>
     *   <li>客户类型特殊（如内部员工、合作伙伴）</li>
     *   <li>批量订单的简化流程</li>
     * </ul>
     *
     * <p>在此状态下，订单履约不受合同限制，但仍需满足其他条件（如支付、库存）。
     */
    NOT_REQUIRED,

    /**
     * 待签署合同。
     *
     * <p>订单需要客户签署合同后才能执行，这是大多数旅游订单的默认状态。
     * 在订单审批通过（APPROVED）后，销售或操作人员需要安排客户签署合同。
     *
     * <p>状态约束：
     * <ul>
     *   <li>在订单主状态为 {@code APPROVED} 时，必须过渡到 {@code SIGNED} 才能出行</li>
     *   <li>若产品配置或客户属性要求合同，则订单创建时自动设置为此状态</li>
     * </ul>
     */
    PENDING_SIGN,

    /**
     * 合同已签署。
     *
     * <p>表示订单合同已由客户正式签署并生效，相关电子文档已归档。
     * 这是订单可以确认出行（IN_TRAVEL）的必要条件之一。
     *
     * <p>业务影响：
     * <ul>
     *   <li>触发保险购买、出团任务分配等后续流程</li>
     *   <li>合同条款成为履约和争议处理的依据</li>
     *   <li>在订单完成前不可修改合同内容</li>
     * </ul>
     *
     * <p>状态变更：从 {@code PENDING_SIGN} 经合同签署操作后变为 {@code SIGNED}。
     */
    SIGNED
}