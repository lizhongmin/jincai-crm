<template>
  <div class="report-page">
    <a-card class="section-card" :bordered="false">
      <template #title>经营分析</template>
      <template #extra>
        <a-space>
          <a-button @click="load">刷新</a-button>
          <a-button @click="exportFunnel">导出漏斗</a-button>
          <a-button @click="exportAging">导出账龄</a-button>
          <a-button type="primary" @click="exportProfit">导出毛利</a-button>
        </a-space>
      </template>

      <div class="grid-2">
        <a-card size="small" title="销售漏斗" :bordered="true">
          <div class="funnel-list">
            <div class="f-item"><span>订单总数</span><strong>{{ funnel.totalOrders || 0 }}</strong></div>
            <div class="f-item"><span>待审批</span><strong>{{ funnel.pendingApproval || 0 }}</strong></div>
            <div class="f-item"><span>已通过</span><strong>{{ funnel.approvedOrders || 0 }}</strong></div>
            <div class="f-item"><span>已完结</span><strong>{{ funnel.completedOrders || 0 }}</strong></div>
          </div>
        </a-card>

        <a-card size="small" title="应收账龄" :bordered="true">
          <div class="aging-list">
            <div class="a-item" v-for="i in agingItems" :key="i.label">
              <span>{{ i.label }}</span>
              <strong>{{ i.value }}</strong>
            </div>
          </div>
        </a-card>
      </div>
    </a-card>

    <a-card class="section-card" :bordered="false" title="线路毛利分布">
      <a-table :columns="columns" :data-source="profit" row-key="routeId" :pagination="{ pageSize: 10 }" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { reportApi } from '../api/crm';
import { notifyError, notifySuccess } from '../utils/notify';

const funnel = ref<any>({});
const aging = ref<any>({});
const profit = ref<any[]>([]);

const columns = [
  { title: '线路', dataIndex: 'routeName' },
  { title: '收入', dataIndex: 'income' },
  { title: '成本', dataIndex: 'cost' },
  { title: '毛利', dataIndex: 'grossProfit' }
];

const agingItems = computed(() => {
  const data = aging.value?.receivableAging || {};
  return [
    { label: '0-30 天', value: data['0-30'] ?? 0 },
    { label: '31-60 天', value: data['31-60'] ?? 0 },
    { label: '61-90 天', value: data['61-90'] ?? 0 },
    { label: '90 天以上', value: data['90+'] ?? 0 }
  ];
});

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

const saveBlob = (blob: Blob, fileName: string) => {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = fileName;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
};

const exportFunnel = async () => {
  try {
    const { data } = await reportApi.exportFunnel();
    saveBlob(data, 'sales-funnel-report.xlsx');
    notifySuccess('销售漏斗导出成功');
  } catch (error) {
    notifyError(error);
  }
};

const exportAging = async () => {
  try {
    const { data } = await reportApi.exportAging();
    saveBlob(data, 'cashflow-aging-report.xlsx');
    notifySuccess('账龄报表导出成功');
  } catch (error) {
    notifyError(error);
  }
};

const exportProfit = async () => {
  try {
    const { data } = await reportApi.exportProfit();
    saveBlob(data, 'profit-report.xlsx');
    notifySuccess('毛利报表导出成功');
  } catch (error) {
    notifyError(error);
  }
};

onMounted(load);
</script>

<style scoped>
.report-page {
  display: grid;
  gap: 16px;
}

.funnel-list,
.aging-list {
  display: grid;
  gap: 10px;
}

.f-item,
.a-item {
  border: 1px solid var(--line);
  border-radius: 10px;
  background: var(--panel-2);
  padding: 10px 12px;
  display: flex;
  justify-content: space-between;
}

.f-item span,
.a-item span {
  color: var(--muted);
}
</style>