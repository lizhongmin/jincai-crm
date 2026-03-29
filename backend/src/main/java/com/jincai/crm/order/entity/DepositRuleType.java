package com.jincai.crm.order.entity;

/**
 * 定金规则类型枚举。
 *
 * <p>定义订单定金的计算方式，与定金金额（depositValue）配合使用，
 * 共同决定客户需支付的定金数额。
 *
 * <p><b>类型对比</b>：
 * <table border="1">
 *   <tr>
 *     <th>类型</th>
 *     <th>depositValue 含义</th>
 *     <th>计算方式</th>
 *     <th>优点</th>
 *     <th>缺点</th>
 *   </tr>
 *   <tr>
 *     <td>PERCENT</td>
 *     <td>百分比（如 30.00 表示 30%）</td>
 *     <td>订单总价 × 百分比</td>
 *     <td>与订单金额成正比，公平合理</td>
 *     <td>金额可能过低或过高</td>
 *   </tr>
 *   <tr>
 *     <td>FIXED</td>
 *     <td>固定金额（单位：元）</td>
 *     <td>固定金额</td>
 *     <td>金额确定，便于客户理解和预算</td>
 *     <td>与订单金额无关，可能不公平</td>
 *   </tr>
 * </table>
 *
 * <p><b>业务规则</b>：
 * <ul>
 *   <li>类型在订单创建时从产品配置继承，可被出团日期配置覆盖</li>
 *   <li>不同类型对应不同的金额合理性检查规则</li>
 *   <li>通常与 {@code OrderPaymentPolicy.DEPOSIT_BALANCE} 配合使用</li>
 * </ul>
 *
 * @see TravelOrder#depositType 订单定金规则类型字段
 * @see TravelOrder#depositValue 订单定金金额/比例字段
 * @see RouteProduct#depositTypeDefault 线路产品默认定金规则类型
 * @see Departure#depositTypeOverride 出团日期定金规则类型覆盖
 */
public enum DepositRuleType {
    /**
     * 按百分比计算定金。
     *
     * <p>定金金额根据订单总价按比例计算，是最公平的定金方式：
     * <ul>
     *   <li>{@code depositValue} 表示百分比，如 30.00 表示 30%</li>
     *   <li>计算公式：定金 = 订单总价 × depositValue%</li>
     *   <li>订单金额越高，定金金额越高，风险分摊合理</li>
     * </ul>
     *
     * <p>适用场景：
     * <ul>
     *   <li>标准化的旅游产品，价格差异较大</li>
     *   <li>希望定金与订单价值成正比的场景</li>
     *   <li>没有明显成本差异的产品线</li>
     * </ul>
     *
     * <p>配置建议：
     * <ul>
     *   <li>通常设置在 10%-50% 之间</li>
     *   <li>避免设置过高（如 > 50%）影响客户支付意愿</li>
     *   <li>避免设置过低（如 < 10%）失去筛选和风险控制作用</li>
     * </ul>
     *
     * <p>风险控制：
     * <ul>
     *   <li>设置最低定金金额，防止过低</li>
     *   <li>设置最高定金比例，防止过高</li>
     *   <li>根据产品类型和客户群体制定差异化策略</li>
     * </ul>
     */
    PERCENT,

    /**
     * 按固定金额计算定金。
     *
     * <p>定金金额为固定值，与订单总价无关，便于客户理解和预算：
     * <ul>
     *   <li>{@code depositValue} 表示固定金额，单位为元</li>
     *   <li>计算公式：定金 = depositValue（固定值）</li>
     *   <li>订单金额高低不影响定金金额，客户预算明确</li>
     * </ul>
     *
     * <p>适用场景：
     * <ul>
     *   <li>成本结构相对固定的产品（如机票+酒店）</li>
     *   <li>客户对价格敏感度低，更关注服务确定性</li>
     *   <li>需要简化定价策略，减少客户选择困难</li>
     * </ul>
     *
     * <p>配置建议：
     * <ul>
     *   <li>根据产品成本和市场竞争情况设置</li>
     *   <li>同一产品线保持定金金额一致</li>
     *   <li>避免与订单总价差异过大（如定金 > 订单总价）</li>
     * </ul>
     *
     * <p>风险控制：
     * <ul>
     *   <li>高价订单定金可能偏低，需额外风险控制</li>
     *   <li>低价订单定金可能偏高，影响销售转化</li>
     *   <li>建议与百分比方式结合使用，按产品类型区分</li>
     * </ul>
     */
    FIXED
}