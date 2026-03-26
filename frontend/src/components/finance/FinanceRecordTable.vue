<template>
  <pro-table :columns="columns" :data-source="props.items" row-key="id" :pagination="false" :scroll="{ x: 'max-content' }">
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'status'">
        <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
      </template>
      <template v-else-if="column.dataIndex === 'actions'">
        <a-space>
          <a-button
            v-if="props.canReviewPermission"
            size="small"
            type="primary"
            :disabled="record.status === 'APPROVED'"
            @click="emit('review', record, true)"
          >
            通过
          </a-button>
          <a-button
            v-if="props.canReviewPermission"
            size="small"
            danger
            :disabled="record.status === 'REJECTED'"
            @click="emit('review', record, false)"
          >
            驳回
          </a-button>
        </a-space>
      </template>
    </template>
  </a-table>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(defineProps<{
  mode: 'receipt' | 'payment';
  items: any[];
  canReviewPermission?: boolean;
}>(), {
  canReviewPermission: true
});

const emit = defineEmits<{
  (e: 'review', record: any, approved: boolean): void;
}>();

const columns = computed(() => [
  { title: '金额', dataIndex: 'amount', width: 120 },
  { title: '状态', dataIndex: 'status', width: 140 },
  { title: '备注', dataIndex: 'remark' },
  { title: '创建时间', dataIndex: 'createdAt', width: 180 },
  { title: '操作', dataIndex: 'actions', width: 140 }
]);

const statusColor = (status: string) => ({
  PENDING_REVIEW: 'orange',
  APPROVED: 'green',
  REJECTED: 'red'
}[status] || 'default');

const statusLabel = (status: string) => ({
  PENDING_REVIEW: '待审核',
  APPROVED: '已通过',
  REJECTED: '已驳回'
}[status] || status);
</script>
