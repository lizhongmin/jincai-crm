<template>
  <a-card class="summary-bar-card">
    <a-row :gutter="16">
      <a-col :span="6">
        <a-statistic
          title="应收合计"
          :value="receivableTotal"
          :precision="2"
          :value-style="{ color: '#1890ff' }"
        >
          <template #prefix>
            <DollarCircleTwoTone two-tone-color="#1890ff" />
          </template>
        </a-statistic>
      </a-col>
      <a-col :span="6">
        <a-statistic
          title="实收合计"
          :value="receivedTotal"
          :precision="2"
          :value-style="{ color: '#52c41a' }"
        >
          <template #prefix>
            <CheckCircleTwoTone two-tone-color="#52c41a" />
          </template>
        </a-statistic>
      </a-col>
      <a-col :span="6">
        <a-statistic
          title="应付合计"
          :value="payableTotal"
          :precision="2"
          :value-style="{ color: '#fa8c16' }"
        >
          <template #prefix>
            <AccountBookTwoTone two-tone-color="#fa8c16" />
          </template>
        </a-statistic>
      </a-col>
      <a-col :span="6">
        <a-statistic
          title="退款合计"
          :value="refundTotal"
          :precision="2"
          :value-style="{ color: '#ff4d4f' }"
        >
          <template #prefix>
            <RedoOutlined style="color: #ff4d4f" />
          </template>
        </a-statistic>
      </a-col>
    </a-row>
  </a-card>
</template>

<script setup lang="ts">
import { DollarCircleTwoTone, CheckCircleTwoTone, AccountBookTwoTone } from '@ant-design/icons-vue';
import { RedoOutlined } from '@ant-design/icons-vue';

defineProps<{
  receivableTotal: number;
  receivedTotal: number;
  payableTotal: number;
  refundTotal: number;
}>();
</script>

<style scoped>
.summary-bar-card {
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  border-top: 4px solid #1890ff;
}

.summary-bar-card :deep(.ant-statistic) {
  text-align: center;
}

.summary-bar-card :deep(.ant-statistic-title) {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.summary-bar-card :deep(.ant-statistic-content) {
  font-size: 20px;
  font-weight: 500;
}
</style>