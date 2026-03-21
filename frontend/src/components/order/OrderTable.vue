<template>
  <a-table
    :columns="columns"
    :data-source="items"
    row-key="id"
    :pagination="{ pageSize: 8 }"
    :custom-row="customRow"
  >
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'status'">
        <a-tag :color="statusColor(record.status)">{{ record.status }}</a-tag>
      </template>
      <template v-else-if="column.dataIndex === 'actions'">
        <a-space wrap>
          <a-button size="small" @click.stop="emit('view', record)">详情</a-button>
          <a-button size="small" :disabled="!canEdit(record)" @click.stop="emit('edit', record)">编辑</a-button>
          <a-button size="small" :disabled="!canSubmit(record)" @click.stop="emit('submit', record)">提交</a-button>
          <a-button size="small" type="primary" :disabled="!canReview(record)" @click.stop="emit('approve', record)">通过</a-button>
          <a-button size="small" danger :disabled="!canReview(record)" @click.stop="emit('reject', record)">驳回</a-button>
          <a-popconfirm title="确认删除该订单？" @confirm="emit('remove', record)">
            <a-button size="small" danger ghost :disabled="!canEdit(record)" @click.stop>删除</a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </template>
  </a-table>
</template>

<script setup lang="ts">
const props = defineProps<{ items: any[] }>();

const emit = defineEmits<{
  (e: 'view', record: any): void;
  (e: 'edit', record: any): void;
  (e: 'submit', record: any): void;
  (e: 'approve', record: any): void;
  (e: 'reject', record: any): void;
  (e: 'remove', record: any): void;
}>();

const columns = [
  { title: '订单号', dataIndex: 'orderNo', width: 220 },
  { title: '客户', dataIndex: 'customerName', width: 160 },
  { title: '线路', dataIndex: 'routeName', width: 160 },
  { title: '团期', dataIndex: 'departureLabel', width: 200 },
  { title: '类型', dataIndex: 'orderType', width: 100 },
  { title: '金额', dataIndex: 'totalAmount', width: 120 },
  { title: '状态', dataIndex: 'status', width: 160 },
  { title: '操作', dataIndex: 'actions', width: 360, fixed: 'right' }
];

const statusColor = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: 'default',
    PENDING_APPROVAL: 'orange',
    APPROVED: 'blue',
    REJECTED: 'red',
    FINANCE_IN_PROGRESS: 'purple',
    COMPLETED: 'green'
  };
  return map[status] || 'default';
};

const canEdit = (record: any) => ['DRAFT', 'REJECTED'].includes(record.status);
const canSubmit = (record: any) => ['DRAFT', 'REJECTED'].includes(record.status);
const canReview = (record: any) => record.status === 'PENDING_APPROVAL';

const customRow = (record: any) => ({
  onClick: () => emit('view', record)
});
</script>