<template>
  <a-card size="small" :title="title" :bordered="true">
    <a-table :columns="columns" :data-source="items" row-key="id" size="small" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="statusColor(record.status)">{{ record.status }}</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'actions'">
          <a-space wrap>
            <template v-if="mode === 'receivable' || mode === 'payable'">
              <a-button size="small" type="link" @click="emit('primary', record)">{{ mode === 'receivable' ? '收款' : '付款' }}</a-button>
              <a-button size="small" type="link" @click="emit('secondary', record)">记录</a-button>
            </template>
            <template v-if="mode === 'refund'">
              <a-button size="small" type="primary" :disabled="record.status === 'APPROVED'" @click="emit('approve', record)">通过</a-button>
              <a-button size="small" danger :disabled="record.status === 'REJECTED'" @click="emit('reject', record)">驳回</a-button>
            </template>
            <a-button size="small" @click="emit('edit', record)">编辑</a-button>
            <a-popconfirm title="确认删除这条记录？" @confirm="emit('remove', record)">
              <a-button size="small" danger ghost>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  title: string;
  mode: 'receivable' | 'payable' | 'refund';
  items: any[];
}>();

const emit = defineEmits<{
  (e: 'primary', record: any): void;
  (e: 'secondary', record: any): void;
  (e: 'approve', record: any): void;
  (e: 'reject', record: any): void;
  (e: 'edit', record: any): void;
  (e: 'remove', record: any): void;
}>();

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
</script>