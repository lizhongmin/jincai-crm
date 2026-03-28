<template>
  <a-card size="small" :bordered="true" :class="cardClass">
    <template #title>
      <span class="table-title">{{ props.title }}</span>
    </template>
    <a-table :columns="columns" :data-source="props.items" row-key="id" size="small" :pagination="false" :scroll="{ x: 760 }">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'actions'">
          <a-space wrap>
            <template v-if="props.mode === 'receivable' || props.mode === 'payable'">
              <a-tooltip :title="props.mode === 'receivable' ? '收款' : '付款'">
                <a-button size="small" type="link" @click="emit('primary', record)">{{ props.mode === 'receivable' ? '收款' : '付款' }}</a-button>
              </a-tooltip>
              <a-tooltip title="查看记录">
                <a-button size="small" type="link" @click="emit('secondary', record)">记录</a-button>
              </a-tooltip>
            </template>
            <template v-if="props.mode === 'refund'">
              <a-tooltip title="审核通过">
                <a-button
                  v-if="props.canReviewPermission"
                  size="small"
                  type="primary"
                  :disabled="record.status === 'APPROVED'"
                  @click="emit('approve', record)"
                >
                  通过
                </a-button>
              </a-tooltip>
              <a-tooltip title="审核驳回">
                <a-button
                  v-if="props.canReviewPermission"
                  size="small"
                  danger
                  :disabled="record.status === 'REJECTED'"
                  @click="emit('reject', record)"
                >
                  驳回
                </a-button>
              </a-tooltip>
            </template>
            <a-tooltip title="编辑">
              <a-button size="small" @click="emit('edit', record)">编辑</a-button>
            </a-tooltip>
            <a-tooltip title="删除">
              <a-popconfirm title="确认删除这条记录？" @confirm="emit('remove', record)">
                <a-button size="small" danger ghost>删除</a-button>
              </a-popconfirm>
            </a-tooltip>
          </a-space>
        </template>
      </template>
    </a-table>
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Tooltip as ATooltip } from 'ant-design-vue';

const props = withDefaults(defineProps<{
  title: string;
  mode: 'receivable' | 'payable' | 'refund';
  items: any[];
  canReviewPermission?: boolean;
}>(), {
  canReviewPermission: true
});

const emit = defineEmits<{
  (e: 'primary', record: any): void;
  (e: 'secondary', record: any): void;
  (e: 'approve', record: any): void;
  (e: 'reject', record: any): void;
  (e: 'edit', record: any): void;
  (e: 'remove', record: any): void;
}>();

const cardClass = computed(() => {
  return {
    'receivable-card': props.mode === 'receivable',
    'payable-card': props.mode === 'payable',
    'refund-card': props.mode === 'refund'
  };
});

const columns = computed(() => {
  if (props.mode === 'receivable') {
    return [
      { title: '项目', dataIndex: 'itemName' },
      { title: '应收', dataIndex: 'amount', width: 110 },
      { title: '已收', dataIndex: 'received', width: 110 },
      { title: '状态', dataIndex: 'status', width: 120 },
      { title: '操作', dataIndex: 'actions', width: 240 }
    ];
  }
  if (props.mode === 'payable') {
    return [
      { title: '项目', dataIndex: 'itemName' },
      { title: '应付', dataIndex: 'amount', width: 110 },
      { title: '已付', dataIndex: 'paid', width: 110 },
      { title: '状态', dataIndex: 'status', width: 120 },
      { title: '操作', dataIndex: 'actions', width: 240 }
    ];
  }
  return [
    { title: '退款金额', dataIndex: 'amount', width: 110 },
    { title: '退款原因', dataIndex: 'reason' },
    { title: '状态', dataIndex: 'status', width: 140 },
    { title: '操作', dataIndex: 'actions', width: 260 }
  ];
});

const statusColor = (status: string) => {
  const map: Record<string, string> = {
    OPEN: 'blue',
    CLOSED: 'green',
    PENDING_REVIEW: 'orange',
    APPROVED: 'green',
    REJECTED: 'red'
  };
  return map[status] || 'default';
};

const statusLabel = (status: string) => ({
  OPEN: '进行中',
  CLOSED: '已关闭',
  PENDING_REVIEW: '待审核',
  APPROVED: '已通过',
  REJECTED: '已驳回'
}[status] || status);
</script>

<style scoped>
.receivable-card :deep(.ant-card-head) {
  border-left: 4px solid #1890ff;
  padding-left: 12px;
}

.payable-card :deep(.ant-card-head) {
  border-left: 4px solid #fa8c16;
  padding-left: 12px;
}

.refund-card :deep(.ant-card-head) {
  border-left: 4px solid #ff4d4f;
  padding-left: 12px;
}

.table-title {
  font-weight: 500;
}

/* 表格样式优化 */
:deep(.ant-table) {
  font-size: 12px;
}

:deep(.ant-table-thead > tr > th) {
  font-size: 12px;
  font-weight: 500;
  padding: 8px 8px;
}

:deep(.ant-table-tbody > tr > td) {
  padding: 8px 8px;
  font-size: 12px;
}

/* 操作按钮紧凑排列 */
:deep(.ant-space) {
  gap: 4px !important;
}

:deep(.ant-btn) {
  font-size: 12px;
  padding: 0 7px;
  height: 24px;
}

/* 状态标签样式优化 */
:deep(.ant-tag) {
  font-size: 12px;
  padding: 0 7px;
  line-height: 20px;
}
</style>
