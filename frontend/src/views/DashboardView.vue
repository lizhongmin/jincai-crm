<template>
  <div class="dashboard">
    <div class="metric-grid">
      <a-card class="section-card metric" :bordered="false">
        <span>订单总数</span>
        <strong>{{ funnel.totalOrders || 0 }}</strong>
      </a-card>
      <a-card class="section-card metric" :bordered="false">
        <span>待审批</span>
        <strong>{{ funnel.pendingApproval || 0 }}</strong>
      </a-card>
      <a-card class="section-card metric" :bordered="false">
        <span>已通过</span>
        <strong>{{ funnel.approvedOrders || 0 }}</strong>
      </a-card>
      <a-card class="section-card metric" :bordered="false">
        <span>已完结</span>
        <strong>{{ funnel.completedOrders || 0 }}</strong>
      </a-card>
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

    <a-card class="section-card" :bordered="false" title="线路毛利 Top">
      <a-table :columns="profitCols" :data-source="profitTop" row-key="routeId" :pagination="false" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { reportApi } from '../api/crm';
import { notifyError } from '../utils/notify';

const funnel = ref<any>({});
const aging = ref<any>({});
const profit = ref<any[]>([]);

const agingRows = computed(() => {
  const receivableAging = aging.value?.receivableAging || {};
  return [
    { label: '0-30 天', value: receivableAging['0-30'] ?? 0 },
    { label: '31-60 天', value: receivableAging['31-60'] ?? 0 },
    { label: '61-90 天', value: receivableAging['61-90'] ?? 0 },
    { label: '90 天以上', value: receivableAging['90+'] ?? 0 }
  ];
});

const profitTop = computed(() => [...profit.value].slice(0, 8));

const profitCols = [
  { title: '线路', dataIndex: 'routeName' },
  { title: '收入', dataIndex: 'income' },
  { title: '成本', dataIndex: 'cost' },
  { title: '毛利', dataIndex: 'grossProfit' }
];

const load = async () => {
  try {
    const [f, a, p] = await Promise.all([reportApi.funnel(), reportApi.aging(), reportApi.profit()]);
    funnel.value = f.data.data || {};
    aging.value = a.data.data || {};
    profit.value = p.data.data || [];
  } catch (error) {
    notifyError(error);
  }
};

onMounted(load);
</script>

<style scoped>
.dashboard {
  display: grid;
  gap: 16px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.metric span {
  color: var(--muted);
  font-size: 13px;
}

.metric strong {
  margin-top: 10px;
  display: block;
  font-size: 30px;
  line-height: 1;
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
