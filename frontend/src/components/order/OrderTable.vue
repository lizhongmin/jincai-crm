<script setup lang="ts">
import { computed } from 'vue';
import ProTable from '../common/ProTable.vue';
import {
  CONTRACT_STATUS_LABEL_MAP,
  INVENTORY_STATUS_LABEL_MAP,
  ORDER_STATUS_LABEL_MAP,
  PAYMENT_STATUS_LABEL_MAP,
  SETTLEMENT_STATUS_LABEL_MAP,
  enumLabel
} from '../../constants/display';

type OrderAction =
  | 'SUBMIT'
  | 'RESUBMIT'
  | 'WITHDRAW'
  | 'APPROVE'
  | 'REJECT'
  | 'SIGN_CONTRACT'
  | 'LOCK_INVENTORY'
  | 'MARK_IN_TRAVEL'
  | 'MARK_TRAVEL_FINISHED'
  | 'CANCEL';

const props = withDefaults(defineProps<{ items: any[]; canReviewPermission?: boolean }>(), {
  canReviewPermission: true
});

const emit = defineEmits<{
  (e: 'view', record: any): void;
  (e: 'edit', record: any): void;
  (e: 'action', record: any, action: OrderAction): void;
  (e: 'remove', record: any): void;
  (e: 'table-change', pagination: { current?: number; pageSize?: number }): void;
}>();

const baseColumns = [
  { title: '订单号', dataIndex: 'orderNo', ellipsis: true, width: 210 },
  { title: '客户', dataIndex: 'customerName', ellipsis: true, minWidth: 120 },
  { title: '线路', dataIndex: 'routeName', ellipsis: true, minWidth: 150 },
  { title: '团期', dataIndex: 'departureLabel', minWidth: 200 },
  { title: '金额', dataIndex: 'totalAmount', width: 90 },
  { title: '主状态', dataIndex: 'status', width: 90 },
  { title: '签约', dataIndex: 'contractStatus', width: 90 },
  { title: '收款', dataIndex: 'paymentStatus', width: 90 },
  { title: '锁位', dataIndex: 'inventoryStatus', width: 90 },
  { title: '结算', dataIndex: 'settlementStatus', width: 90 },
  { title: '操作', dataIndex: 'actions', width: 260, fixed: 'right' as const }
];

const columns = baseColumns;

const statusLabelMap: Record<string, string> = {
  ...ORDER_STATUS_LABEL_MAP,
  ...CONTRACT_STATUS_LABEL_MAP,
  ...PAYMENT_STATUS_LABEL_MAP,
  ...INVENTORY_STATUS_LABEL_MAP,
  ...SETTLEMENT_STATUS_LABEL_MAP
};

const statusColorMap: Record<string, string> = {
  DRAFT: 'default',
  PENDING_APPROVAL: 'orange',
  APPROVED: 'blue',
  REJECTED: 'red',
  IN_TRAVEL: 'processing',
  TRAVEL_FINISHED: 'purple',
  SETTLING: 'gold',
  COMPLETED: 'green',
  CANCELED: 'default',
  PENDING_SIGN: 'orange',
  SIGNED: 'green',
  NOT_REQUIRED: 'default',
  UNPAID: 'default',
  PARTIAL: 'blue',
  PAID: 'green',
  REFUNDING: 'volcano',
  REFUNDED: 'default',
  UNLOCKED: 'default',
  LOCKED: 'green',
  RELEASED: 'purple',
  UNSETTLED: 'default',
  SETTLED: 'green'
};

const statusLabel = (status?: string) => enumLabel(statusLabelMap, status);
const statusColor = (status?: string) => statusColorMap[status || ''] || 'default';

const canEdit = (record: any) => ['DRAFT', 'REJECTED'].includes(record.status);
const canDelete = (record: any) => ['DRAFT', 'REJECTED'].includes(record.status);

const quickAction = (record: any): OrderAction | null => {
  if (record.status === 'DRAFT') return 'SUBMIT';
  if (record.status === 'REJECTED') return 'RESUBMIT';
  if (record.status === 'PENDING_APPROVAL' && props.canReviewPermission) return 'APPROVE';
  if (record.status === 'APPROVED' && record.contractStatus === 'PENDING_SIGN') return 'SIGN_CONTRACT';
  if ((record.status === 'APPROVED' || record.status === 'SETTLING') && record.inventoryStatus !== 'LOCKED') return 'LOCK_INVENTORY';
  if ((record.status === 'APPROVED' || record.status === 'SETTLING') && record.inventoryStatus === 'LOCKED') return 'MARK_IN_TRAVEL';
  if (record.status === 'IN_TRAVEL') return 'MARK_TRAVEL_FINISHED';
  return null;
};

const quickActionLabel = (action: OrderAction | null) => {
  const labels: Record<OrderAction, string> = {
    SUBMIT: '提交',
    RESUBMIT: '重提',
    WITHDRAW: '撤回',
    APPROVE: '通过',
    REJECT: '驳回',
    SIGN_CONTRACT: '签约',
    LOCK_INVENTORY: '锁位',
    MARK_IN_TRAVEL: '出团',
    MARK_TRAVEL_FINISHED: '回团',
    CANCEL: '取消'
  };
  return action ? labels[action] : '';
};

const moreActions = (record: any) => {
  const actions: Array<{ key: OrderAction; label: string }> = [];
  if (record.status === 'PENDING_APPROVAL') actions.push({ key: 'WITHDRAW', label: '撤回审批' });
  if (record.status === 'PENDING_APPROVAL' && props.canReviewPermission) actions.push({ key: 'REJECT', label: '审批驳回' });
  if (!['COMPLETED', 'CANCELED', 'IN_TRAVEL'].includes(record.status)) actions.push({ key: 'CANCEL', label: '取消订单' });
  return actions;
};

const tableScroll = computed(() => ({ x: 'max-content' }));

const customRow = (record: any) => ({
  onClick: () => emit('view', record)
});

const onTableChange = (pagination: { current?: number; pageSize?: number }) => {
  emit('table-change', pagination);
};
</script>

<template>
  <pro-table
    :columns="columns"
    :data-source="props.items"
    row-key="id"
    :pagination="false"
    :scroll="tableScroll"
    :custom-row="customRow"
    @change="onTableChange"
  >
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'status'">
        <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
      </template>
      <template v-else-if="column.dataIndex === 'departureLabel'">
        <div class="departure-cell">
          <div class="departure-name">{{ record.departureName || record.departureLabel || '-' }}</div>
          <div class="departure-meta">{{ record.departureDateRange || '-' }}</div>
        </div>
      </template>
      <template v-else-if="['contractStatus', 'paymentStatus', 'inventoryStatus', 'settlementStatus'].includes(String(column.dataIndex))">
        <a-tag :color="statusColor(record[column.dataIndex])">{{ statusLabel(record[column.dataIndex]) }}</a-tag>
      </template>
      <template v-else-if="column.dataIndex === 'actions'">
        <div class="action-cell" @click.stop>
          <a-space>
            <a-button size="small" @click="emit('view', record)">详情</a-button>
            <a-button size="small" :disabled="!canEdit(record)" @click="emit('edit', record)">编辑</a-button>
            <a-button
              v-if="quickAction(record)"
              size="small"
              type="primary"
              @click="emit('action', record, quickAction(record)!)"
            >
              {{ quickActionLabel(quickAction(record)) }}
            </a-button>
            <a-dropdown v-if="moreActions(record).length">
              <a-button size="small">更多</a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item
                    v-for="item in moreActions(record)"
                    :key="item.key"
                    @click="emit('action', record, item.key)"
                  >
                    {{ item.label }}
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
            <a-popconfirm title="确认删除该订单？" @confirm="emit('remove', record)">
              <a-button size="small" danger ghost :disabled="!canDelete(record)">删除</a-button>
            </a-popconfirm>
          </a-space>
        </div>
      </template>
    </template>
  </pro-table>
</template>

<style scoped>
.departure-cell {
  display: grid;
  gap: 2px;
  line-height: 1.3;
}

.departure-name {
  font-weight: 600;
  color: #1f2937;
}

.departure-meta {
  font-size: 12px;
  color: #6b7280;
}

.action-cell {
  width: 100%;
  white-space: nowrap;
}

:deep(.ant-table-body) {
  overflow-x: auto !important;
}
</style>
