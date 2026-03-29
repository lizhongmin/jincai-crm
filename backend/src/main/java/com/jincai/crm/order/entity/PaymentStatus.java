package com.jincai.crm.order.entity;

/**
 * 订单支付状态枚举。
 *
 * <p>描述订单在收付款维度的当前状态，与应收（Receivable）和实收（Receipt）
 * 的金额关系紧密相关，是财务对账和风险控制的重要依据。
 *
 * <p><b>状态流转</b>：
 * <pre>
 *    UNPAID（未支付）
 *        │
 *        ▼
 *   PARTIAL（部分支付） ◄─┐
 *        │              │ 退款
 *        ▼              │
 *     PAID（已付清） ────┤
 *        │              │
 *        ▼              │
 *  REFUNDING（退款中） ◄─┘
 *        │
 *        ▼
 *  REFUNDED（已退款）
 * </pre>
 *
 * <p><b>状态定义</b>：
 * <ul>
 *   <li>{@code UNPAID}：订单创建后初始状态，应收款项尚未支付</li>
 *   <li>{@code PARTIAL}：已收到部分款项，但未达到应收总额</li>
 *   <li>{@code PAID}：应收款项已全部收到，订单可进入出行阶段</li>
 *   <li>{@code REFUNDING}：订单发生退款（如取消、变更），退款流程进行中</li>
 *   <li>{@code REFUNDED}：退款已完成，订单款项已全部退还客户</li>
 * </ul>
 *
 * <p><b>业务规则</b>：
 * <ul>
 *   <li>只有支付状态为 {@code PAID} 且无退款记录的订单才能确认出行</li>
 *   <li>支付状态变更由财务模块根据收款单（Receipt）更新</li>
 *   <li>发生退款时，状态可能从 {@code PAID} → {@code REFUNDING} → {@code REFUNDED}</li>
 *   <li>部分退款时，状态可能在 {@code PAID} 和 {@code REFUNDING} 之间切换</li>
 * </ul>
 *
 * @see TravelOrder#paymentStatus 订单支付状态字段
 * @see Receivable 应收款项实体
 * @see Receipt 收款单实体
 * @see Refund 退款单实体
 */
public enum PaymentStatus {
    /**
     * 未支付状态。
     *
     * <p>订单创建后的初始支付状态，表示：
     * <ul>
     *   <li>尚未收到任何款项</li>
     *   <li>应收款项已生成但未到账</li>
     *   <li>订单不满足出行条件</li>
     * </ul>
     *
     * <p>前置状态：无（初始状态）
     * <p>后继状态：{@code PARTIAL}（收到部分款项）或 {@code PAID}（收到全款）
     */
    UNPAID,

    /**
     * 部分支付状态。
     *
     * <p>表示订单已收到部分款项，但未达到应收总额：
     * <ul>
     *   <li>通常发生在定金支付阶段</li>
     *   <li>根据付款策略（PaymentPolicy）决定是否可出行</li>
     *   <li>需持续跟踪剩余款项到账情况</li>
     * </ul>
     *
     * <p>前置状态：{@code UNPAID}（首次收款）或 {@code PAID}（发生部分退款）
     * <p>后继状态：{@code PAID}（收到尾款）或 {@code UNPAID}（全额退款）
     */
    PARTIAL,

    /**
     * 已付清状态。
     *
     * <p>表示订单应收款项已全部收到，是订单可出行的重要前提：
     * <ul>
     *   <li>满足订单付款策略要求</li>
     *   <li>可进入合同签署或出行确认流程</li>
     *   <li>财务对账状态为平账</li>
     * </ul>
     *
     * <p>前置状态：{@code PARTIAL}（收到尾款）或 {@code UNPAID}（一次性付清）
     * <p>后继状态：{@code REFUNDING}（发生退款）或 {@code PARTIAL}（部分退款）
     */
    PAID,

    /**
     * 退款中状态。
     *
     * <p>表示订单因取消、变更等原因正在执行退款流程：
     * <ul>
     *   <li>已有退款申请单（Refund）创建并提交审核</li>
     *   <li>退款尚未实际到账客户账户</li>
     *   <li>订单履约可能暂停（如出行前取消）</li>
     * </ul>
     *
     * <p>前置状态：{@code PAID}（审批通过的退款申请）
     * <p>后继状态：{@code REFUNDED}（退款完成）或 {@code PAID}（退款被拒绝）
     */
    REFUNDING,

    /**
     * 已退款状态。
     *
     * <p>表示订单退款已全部完成，款项已退还给客户：
     * <ul>
     *   <li>所有退款单（Refund）已审核通过并执行</li>
     *   <li>订单财务状态关闭，不再产生新的收付款</li>
     *   <li>通常与订单取消（CANCELED）状态并存</li>
     * </ul>
     *
     * <p>前置状态：{@code REFUNDING}（退款执行完成）
     * <p>后继状态：无（终态）
     */
    REFUNDED
}