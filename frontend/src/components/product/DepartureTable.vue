<template>
  <a-table :columns="columns" :data-source="items" row-key="id">
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'groupSize'">
        {{ record.minGroupSize || 0 }} / {{ record.maxGroupSize || '不限' }}
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
  </a-table>
</template>

<script setup lang="ts">
defineProps<{ items: any[] }>();
defineEmits<{
  (event: 'view', value: any): void;
  (event: 'edit', value: any): void;
  (event: 'remove', value: any): void;
}>();

const columns = [
  { title: '团期编码', dataIndex: 'code', width: 160 },
  { title: '线路', dataIndex: 'routeName', width: 160, customRender: ({ text }: any) => text || '-' },
  { title: '出发日期', dataIndex: 'startDate', width: 120 },
  { title: '截止报名', dataIndex: 'registrationDeadline', width: 120 },
  { title: '库存', dataIndex: 'stock', width: 90 },
  { title: '成团人数', dataIndex: 'groupSize', width: 120 },
  { title: '状态', dataIndex: 'status', width: 110 },
  { title: '操作', dataIndex: 'actions', width: 180 }
];
</script>