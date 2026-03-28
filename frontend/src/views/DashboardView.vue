<template>
  <div class="dashboard">
    <!-- 核心指标概览区域 -->
    <div class="metric-grid biz-summary">
      <div class="item">
        <span class="label">订单总数</span>
        <strong class="value">{{ funnel.totalOrders || 0 }}</strong>
      </div>
      <div class="item">
        <span class="label">待审批</span>
        <strong class="value">{{ funnel.pendingApproval || 0 }}</strong>
      </div>
      <div class="item">
        <span class="label">已审批</span>
        <strong class="value">{{ funnel.approvedOrders || 0 }}</strong>
      </div>
      <div class="item">
        <span class="label">已完结</span>
        <strong class="value">{{ funnel.completedOrders || 0 }}</strong>
      </div>
    </div>

    <div class="grid-2">
      <a-card class="section-card" :bordered="false" title="销售转化率">
        <a-progress
          :percent="Number(((funnel.conversionRate || 0) * 100).toFixed(2))"
          stroke-color="#1677ff"
          status="active"
        />
        <p class="desc">当前完结率：{{ Number(((funnel.conversionRate || 0) * 100).toFixed(2)) }}%</p>
      </a-card>

      <a-card class="section-card" :bordered="false" title="应收账龄">
        <div class="aging-list">
          <div v-for="item in agingRows" :key="item.label" class="aging-item">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>
      </a-card>
    </div>

    <a-card class="section-card" :bordered="false" title="线路毛利 Top 8">
      <a-table :columns="profitCols" :data-source="profitTop" row-key="routeId" :pagination="false" :scroll="{ x: 720 }" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { reportApi } from '../api/crm';
import { notifyError } from '../utils/notify';

/**
 * 仪表板数据模型
 * funnel: 销售漏斗数据，包含订单状态统计和转化率
 * aging: 账龄分析数据，按账龄区间统计应收款
 * profit: 线路利润数据，用于展示毛利排行
 */
const funnel = ref<any>({});
const aging = ref<any>({});
const profit = ref<any[]>([]);

/**
 * 格式化账龄数据为展示用格式
 * 将后端返回的账龄区间数据转换为固定的展示顺序
 */
const agingRows = computed(() => {
  const receivableAging = aging.value?.receivableAging || {};
  return [
    { label: '0-30 天', value: receivableAging['0-30'] ?? 0 },
    { label: '31-60 天', value: receivableAging['31-60'] ?? 0 },
    { label: '61-90 天', value: receivableAging['61-90'] ?? 0 },
    { label: '90 天以上', value: receivableAging['90+'] ?? 0 }
  ];
});

/**
 * 计算顶级利润线路
 * 只展示前8条高毛利线路
 */
const profitTop = computed(() => [...profit.value].slice(0, 8));

/**
 * 线路毛利表格列配置
 * 显示线路名称、收入、成本和毛利信息
 */
const profitCols = [
  { title: '线路', dataIndex: 'routeName' },
  { title: '收入', dataIndex: 'income' },
  { title: '成本', dataIndex: 'cost' },
  { title: '毛利', dataIndex: 'grossProfit' }
];

/**
 * 加载仪表板数据
 * 并行发起三个报表API请求以提高加载性能
 */
const load = async () => {
  try {
    const [f, a, p] = await Promise.all([
      reportApi.funnel(),    // 销售漏斗数据
      reportApi.aging(),     // 账龄分析数据
      reportApi.profit()     // 利润分析数据
    ]);
    funnel.value = f.data.data || {};
    aging.value = a.data.data || {};
    profit.value = p.data.data || [];
  } catch (error) {
    notifyError(error);
  }
};

/**
 * 组件挂载时自动加载数据
 */
onMounted(load);
</script>

<style scoped>
.dashboard {
  display: grid;
  gap: 10px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.aging-list {
  display: grid;
  gap: 8px;
}

.aging-item {
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 10px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.desc {
  color: var(--muted);
  margin: 10px 0 0;
}

@media (max-width: 1080px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>