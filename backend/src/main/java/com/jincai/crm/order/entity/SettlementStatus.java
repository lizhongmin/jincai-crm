package com.jincai.crm.order.entity;

/**
 * 订单结算状态枚举。
 *
 * <p>描述订单在财务结算维度的完成情况，反映应收（Receivable）、
 * 实收（Receipt）、应付（Payable）、实付（Payment）等财务流程的执行状态。
 *
 * <p>与支付状态（PaymentStatus）的区别：
 * <ul>
 *   <li>支付状态：关注客户向旅行社的付款情况</li>
 *   <li>结算状态：关注旅行社整体的财务闭环情况，包括对供应商的付款</li>
 * </ul>
 *
 * <p><b>状态流转</b>：
 * <pre>
 *  UNSETTLED（未结算）
 *      │
 *      ▼
 *   PARTIAL（部分结算） ◄─┐
 *      │                │ 循环结算
 *      ▼                │
 *   SETTLED（已结算） ────┘
 * </pre>
 *
 * <p><b>状态定义</b>：
 * <ul>
 *   <li>{@code UNSETTLED}：订单尚未开始任何结算流程</li>
 *   <li>{@code PARTIAL}：订单部分应收/应付已完成，部分仍在进行中</li>
 *   <li>{@code SETTLED}：订单所有财务事项已结清，可视为财务闭环</li>
 * </ul>
 *
 * <p><b>业务规则</b>：
 * <ul>
 *   <li>只有结算状态为 {@code SETTLED} 的订单才可能转为 {@code COMPLETED} 主状态</li>
 *   <li>结算状态由财务模块根据应收应付的完成情况自动更新</li>
 *   <li>发生退款时，结算状态可能从 {@code SETTLED} 回退到 {@code PARTIAL}</li>
 * </ul>
 *
 * @see TravelOrder#settlementStatus 订单结算状态字段
 * @see PaymentStatus 订单支付状态
 * @see Receivable 应收款项
 * @see Payable 应付款项
 */
public enum SettlementStatus {
    /**
     * 未结算状态。
     *
     * <p>订单财务流程的初始状态，表示：
     * <ul>
     *   <li>应收款项已生成但未收到任何款项</li>
     *   <li>应付款项尚未创建或未支付任何款项</li>
     *   <li>订单不满足财务闭环条件</li>
     * </ul>
     *
     * <p>常见场景：
     * <ul>
     *   <li>订单刚创建完成（已审批），但客户尚未付款</li>
     *   <li>订单处于草稿或审批阶段，未触发财务流程</li>
     * </ul>
     *
     * <p>前置状态：无（初始状态）
     * <p>后继状态：{@code PARTIAL}（开始有财务流动）或 {@code SETTLED}（一次性完成）
     */
    UNSETTLED,

    /**
     * 部分结算状态。
     *
     * <p>表示订单财务流程正在进行中，部分款项已处理：
     * <ul>
     *   <li>已收到部分客户付款，但未全额收款</li>
     *   <li>已支付部分供应商款项，但未全额付款</li>
     *   <li>有退款申请已提交但未完成</li>
     *   <li>财务审核流程尚未全部完成</li>
     * </ul>
     *
     * <p>此状态是订单财务流程的中间态，需持续跟踪直至完成。
     *
     * <p>前置状态：{@code UNSETTLED}（首次财务流动）或 {@code SETTLED}（发生退款）
     * <p>后继状态：{@code SETTLED}（所有财务事项完成）或 {@code UNSETTLED}（全额撤销）
     */
    PARTIAL,

    /**
     * 已结算状态。
     *
     * <p>表示订单所有财务事项已处理完毕，达到财务闭环：
     * <ul>
     *   <li>所有应收款项已收到（或确认无法收回）</li>
     *   <li>所有应付款项已支付（或确认无需支付）</li>
     *   <li>所有退款流程已完成</li>
     *   <li>财务审核流程全部通过</li>
     * </ul>
     *
     * <p>这是订单财务维度的终态，通常与订单主状态 {@code COMPLETED} 对应。
     *
     * <p>业务影响：
     * <ul>
     *   <li>订单可从 {@code TRAVEL_FINISHED} 或 {@code SETTLING} 转为 {@code COMPLETED}</li>
     *   <li>财务数据可归档，不再进行日常操作</li>
     *   <li>作为业绩统计和财务报表的依据</li>
     * </ul>
     *
     * <p>前置状态：{@code PARTIAL}（完成最后的财务事项）
     * <p>后继状态：通常为终态，如发生特殊退款可能回退至 {@code PARTIAL}
     */
    SETTLED
}