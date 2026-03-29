package com.jincai.crm.order.entity;

/**
 * 订单库存状态枚举。
 *
 * <p>描述订单对出团日期（Departure）库存的占用情况，是控制产品
 * 销售数量和防止超卖的核心机制。
 *
 * <p><b>状态流转</b>：
 * <pre>
 *  UNLOCKED（未锁定）
 *      │
 *      ▼
 *   LOCKED（已锁定） ◄─┐
 *      │              │ 释放库存
 *      ▼              │
 * RELEASED（已释放） ──┘
 * </pre>
 *
 * <p><b>状态定义</b>：
 * <ul>
 *   <li>{@code UNLOCKED}：订单未占用任何库存，通常在草稿或审批阶段</li>
 *   <li>{@code LOCKED}：订单已根据锁定策略占用出团日期库存</li>
 *   <li>{@code RELEASED}：订单库存已释放，可能因取消或完成而释放</li>
 * </ul>
 *
 * <p><b>锁定策略</b>：
 * <ul>
 *   <li>{@code ON_DEPOSIT}：支付定金后锁定库存</li>
 *   <li>{@code ON_APPROVAL}：审批通过后立即锁定库存</li>
 *   <li>{@code MANUAL}：手动控制库存锁定时机</li>
 * </ul>
 *
 * <p><b>业务规则</b>：
 * <ul>
 *   <li>订单确认出行前必须处于 {@code LOCKED} 状态</li>
 *   <li>订单取消时，如已锁定库存则自动释放</li>
 *   <li>订单完成时，锁定库存转为实际消耗</li>
 *   <li>库存不足时，不允许将状态从 {@code UNLOCKED} 转为 {@code LOCKED}</li>
 * </ul>
 *
 * @see TravelOrder#inventoryStatus 订单库存状态字段
 * @see OrderLockPolicy 订单锁定策略
 * @see Departure#stock 出团日期库存字段
 */
public enum InventoryStatus {
    /**
     * 未锁定库存状态。
     *
     * <p>表示订单当前未占用任何出团日期库存，可能处于以下场景：
     * <ul>
     *   <li>订单刚创建，处于草稿状态</li>
     *   <li>订单正在审批中，尚未触发库存锁定</li>
     *   <li>订单采用手动锁定策略，尚未执行锁定操作</li>
     * </ul>
     *
     * <p>在此状态下：
     * <ul>
     *   <li>不影响出团日期的可销售数量</li>
     *   <li>不阻止其他订单占用相同库存</li>
     *   <li>订单无法确认出行（除非采用特殊策略）</li>
     * </ul>
     *
     * <p>前置状态：无（初始状态）或 {@code RELEASED}（重新激活）
     * <p>后继状态：{@code LOCKED}（执行库存锁定）
     */
    UNLOCKED,

    /**
     * 已锁定库存状态。
     *
     * <p>表示订单已根据配置的锁定策略成功占用出团日期库存：
     * <ul>
     *   <li>出团日期的可销售库存已相应扣减</li>
     *   <li>其他订单无法再占用相同库存（防止超卖）</li>
     *   <li>订单已具备确认出行的基本条件</li>
     * </ul>
     *
     * <p>锁定时机取决于订单策略：
     * <ul>
     *   <li>{@code ON_DEPOSIT}：客户支付定金后自动锁定</li>
     *   <li>{@code ON_APPROVAL}：订单审批通过后立即锁定</li>
     *   <li>{@code MANUAL}：操作员手动执行锁定</li>
     * </ul>
     *
     * <p>前置状态：{@code UNLOCKED}（执行锁定操作）
     * <p>后继状态：{@code RELEASED}（订单取消或完成）
     */
    LOCKED,

    /**
     * 已释放库存状态。
     *
     * <p>表示订单原先占用的库存已被释放，可能因以下原因：
     * <ul>
     *   <li>订单被取消，系统自动释放已占用库存</li>
     *   <li>订单完成，库存从"预占"转为"实际消耗"</li>
     *   <li>手动释放库存（特殊情况下）</li>
     * </ul>
     *
     * <p>在此状态下：
     * <ul>
     *   <li>原先占用的出团日期库存已归还</li>
     *   <li>其他订单可重新占用释放的库存</li>
     *   <li>订单不再具备库存相关约束</li>
     * </ul>
     *
     * <p>前置状态：{@code LOCKED}（订单取消或完成）
     * <p>后继状态：通常为终态，如需重新占用需转回 {@code LOCKED}
     */
    RELEASED
}