<template>
  <pro-table :columns="columns" :data-source="items" row-key="id" :scroll="{ x: 'max-content' }">
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'groupSize'">
        {{ record.minGroupSize || 0 }} / {{ record.maxGroupSize || '不限' }}
      </template>
      <template v-else-if="column.dataIndex === 'status'">
        <a-tag>{{ statusLabel(record.status) }}</a-tag>
      </template>
      <template v-else-if="column.dataIndex === 'actions'">
        <a-space>
          <a-button type="link" @click="$emit('view', record)">详情</a-button>
          <a-button type="link" @click="$emit('edit', record)">编辑</a-button>
          <a-popconfirm title="确认删除该团期？" @confirm="$emit('remove', record)">
            <a-button type="link" danger>删除</a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </template>
  </pro-table>
</template>

<script setup lang="ts">
import ProTable from '../common/ProTable.vue';
import { DEPARTURE_STATUS_LABEL_MAP, enumLabel } from '../../constants/display';

defineProps<{ items: any[] }>();
defineEmits<{
  (event: 'view', value: any): void;
  (event: 'edit', value: any): void;
  (event: 'remove', value: any): void;
}>();

const statusLabel = (value?: string) => enumLabel(DEPARTURE_STATUS_LABEL_MAP, value);

const columns = [
  { title: '团期编码', dataIndex: 'code', width: 160 },
  { title: '团期名称', dataIndex: 'name', width: 160, customRender: ({ text }: any) => text || '-' },
  { title: '线路', dataIndex: 'routeName', width: 180, customRender: ({ text }: any) => text || '-' },
  { title: '出发日期', dataIndex: 'startDate', width: 120 },
  { title: '截止报名', dataIndex: 'registrationDeadline', width: 120, customRender: ({ text }: any) => text || '-' },
  { title: '库存', dataIndex: 'stock', width: 90 },
  { title: '成团人数', dataIndex: 'groupSize', width: 120 },
  { title: '状态', dataIndex: 'status', width: 110 },
  { title: '操作', dataIndex: 'actions', width: 180, fixed: 'right' as const }
];
</script>
