package com.jincai.crm.order.entity;

/**
 * 订单主状态枚举。
 *
 * <p>描述旅游订单在整个生命周期中经历的核心业务阶段。
 * 订单主状态与合同状态、支付状态、库存状态、结算状态共同构成多维度状态管理系统。
 *
 * <p><b>状态流转图</b>：
 * <pre>
 *     ┌─────────┐
 *     │  DRAFT  │  ←──────────────────┐
 *     └────┬────┘                      │
 *          │ 提交                      │ 重新提交
 *          ▼                          │
 * ┌──────────────────┐                │
 * │ PENDING_APPROVAL │  ┌─────────────┘
 * └────┬─────────────┘  │
 *      │ 审批通过         │ 审批拒绝
 *      ▼                 ▼
 * ┌─────────┐    ┌──────────┐
 * │ APPROVED │    │ REJECTED │
 * └────┬─────┘    └────┬─────┘
 *      │                 │ 撤回
 *      │                 ▼
 *      │           ┌─────────┐
 *      │           │ DRAFT   │ (再次提交)
 *      │           └─────────┘
 *      │ 确认出行
 *      ▼
 * ┌──────────┐
 * │ IN_TRAVEL │
 * └────┬──────┘
 *      │ 结束行程
 *      ▼
 * ┌──────────────────┐
 * │ TRAVEL_FINISHED  │ ──┐
 * └────┬─────────────┘   │ 结算完成
 *      │                 ▼
 *      │           ┌──────────┐
 *      ├────────→ │ COMPLETED│
 *      │           └──────────┘
 *      │
 *      └────────────────────┐
 *                           │ 取消
 *                           ▼
 *                       ┌─────────┐
 *                       │ CANCELED│
 *                       └─────────┘
 * </pre>
 *
 * <p><b>各状态含义</b>：
 * <ul>
 *   <li>{@code DRAFT}：草稿状态。订单创建后默认在此状态，可修改和删除。</li>
 *   <li>{@code PENDING_APPROVAL}：待审批。订单已提交，等待审批人处理。</li>
 *   <li>{@code APPROVED}：已批准。订单审批通过，可进行后续操作（合同签署、支付等）。</li>
 *   <li>{@code REJECTED}：已驳回。订单被审批人拒绝，销售人员可撤回修改后重新提交。</li>
 *   <li>{@code IN_TRAVEL}：出行中。客户正在按计划旅游。</li>
 *   <li>{@code TRAVEL_FINISHED}：出行结束。旅游行程已结束，等待财务结算完成。</li>
 *   <li>{@code SETTLING}：结算中。订单财务结算工作正在进行，应收应付未清。</li>
 *   <li>{@code COMPLETED}：已完成。订单所有履约和结算工作已完成，流程结束。</li>
 *   <li>{@code CANCELED}：已取消。订单因各种原因被取消，不再继续执行。</li>
 * </ul>
 *
 * <p><b>业务约束</b>：
 * <ul>
 *   <li>只有 {@code DRAFT} 和 {@code REJECTED} 状态的订单可以修改和删除</li>
 *   <li>从 {@code PENDING_APPROVAL} 可以撤回至 {@code DRAFT} 或直接拒绝为 {@code REJECTED}</li>
 *   <li>只有审批通过（{@code APPROVED}）才能进入合同签署阶段</li>
 *   <li>只有 {@code IN_TRAVEL} 状态的订单才能确认行程开始和结束</li>
 *   <li>{@code TRAVEL_FINISHED} 订单需完成所有财务结算后才能转为 {@code COMPLETED}</li>
 * </ul>
 *
 * @see TravelOrder#status 订单主状态字段
 * @see ContractStatus 合同状态
 * @see PaymentStatus 支付状态
 * @see InventoryStatus 库存状态
 * @see SettlementStatus 结算状态
 */
public enum OrderStatus {
    /**
     * 草稿状态。
     *
     * <p>订单创建后的初始状态，此时：
     * <ul>
     *   <li>产品、客户、价格等信息允许修改</li>
     *   <li>不占用库存</li>
     *   <li>不收付款</li>
     *   <li>不触发工作流</li>
     * </ul>
     *
     * <p>从该状态可提交至 {@code PENDING_APPROVAL} 待审批。
     */
    DRAFT,

    /**
     * 待审批状态。
     *
     * <p>订单已提交，正在等待审批处理：
     * <ul>
     *   <li>订单信息锁定，不允许修改</li>
     *   <li>进入工作流审批流程</li>
     *   <li>审批通过后进入 {@code APPROVED} 状态</li>
     *   <li>审批拒绝后进入 {@code REJECTED} 状态</li>
     *   <li>销售人员可撤回至 {@code DRAFT} 状态</li>
     * </ul>
     *
     * <p>此状态通常有超时处理机制（如24小时未处理自动提醒）。
     */
    PENDING_APPROVAL,

    /**
     * 已批准状态。
     *
     * <p>订单审批通过，可以进行后续履约操作：
     * <ul>
     *   <li>可根据锁定策略锁定库存（ON_APPROVAL 立即锁定，ON_DEPOSIT 待定金支付后锁定）</li>
     *   <li>可签署合同（如需要）</li>
     *   <li>可创建应收/应付项</li>
     *   <li>可确认出行</li>
     * </ul>
     *
     * <p>此状态是订单从"销售阶段"进入"履约阶段"的里程碑。
     */
    APPROVED,

    /**
     * 已驳回状态。
     *
     * <p>订单审批未通过，销售需根据意见修改后重新提交。
     *
     * <ul>
     *   <li>退回至 {@code DRAFT} 状态后可修改</li>
     *   <li>记录驳回原因，便于追溯</li>
     *   <li>占用库存会被释放（如果已锁定）</li>
     * </ul>
     */
    REJECTED,

    /**
     * 出行中状态。
     *
     * <p>客户已按计划开始旅游行程，需满足前置条件：
     * <ul>
     *   <li>订单状态为 {@code APPROVED 或 SETTLING}</li>
     *   <li>合同已签署（如需）</li>
     *   <li>库存已锁定</li>
     *   <li>款项已支付（全款或按合同约定）</li>
     * </ul>
     *
     * <p>此状态触发出团任务（导游、保险、应急处理等）。
     */
    IN_TRAVEL,

    /**
     * 出行结束状态。
     *
     * <p>旅游行程已完成，客户已返回：
     * <ul>
     *   <li>需从 {@code IN_TRAVEL} 状态过渡</li>
     *   <li>触发客户满意度调查、回访等流程</li>
     *   <li>开始结算流程（如有应付未付）</li>
     * </ul>
     *
     * <p>此状态是订单从"履约阶段"进入"结算阶段"的转折点。
     */
    TRAVEL_FINISHED,

    /**
     * 结算中状态。
     *
     * <p>订单财务结算工作正在进行，但尚未完成：
     * <ul>
     *   <li>存在应收/应付未结清</li>
     *   <li>或有退款在处理中</li>
     *   <li>财务审核流程未走完</li>
     * </ul>
     *
     * <p>此状态可能在 {@code TRAVEL_FINISHED} 和 {@code COMPLETED} 之间切换，也可能直接从
     * {@code APPROVED} 进入（如需要提前结算）。
     */
    SETTLING,

    /**
     * 已完成状态。
     *
     * <p>订单所有流程结束，是最终的成功状态：
     * <ul>
     *   <li>客户已满意结束行程</li>
     *   <li>所有应收款已收讫，应付款已支付</li>
     *   <li>无未完成的退款或争议</li>
     *   <li>订单归档，不可再做修改</li>
     * </ul>
     *
     * <p>达到此状态的条件：{@code settlementStatus == SETTLED}
     * 且主状态为 {@code TRAVEL_FINISHED} 或 {@code SETTLING}。
     */
    COMPLETED,

    /**
     * 已取消状态。
     *
     * <p>订单因各种原因被取消，可能是：
     * <ul>
     *   <li>客户主动取消（在规定时间内）</li>
     *   <li>销售人员取消（录入错误、需求变更等）</li>
     *   <li>系统自动取消（如超时未支付）</li>
     *   <li>供应商原因无法成团</li>
     * </ul>
     *
     * <p>取消后：
     * <ul>
     *   <li>已锁定库存会释放</li>
     *   <li>可能需要产生退款流程</li>
     *   <li>记录取消原因，用于统计分析</li>
     * </ul>
     */
    CANCELED
}