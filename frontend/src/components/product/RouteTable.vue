<template>
  <a-table :columns="columns" :data-source="items" row-key="id">
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'duration'">
        {{ record.durationDays || '-' }}天{{ record.durationNights ?? '-' }}晚
      </template>
      <template v-else-if="column.dataIndex === 'actions'">
        <a-space>
          <a-button type="link" @click="$emit('view', record)">详情</a-button>
          <a-button type="link" @click="$emit('edit', record)">编辑</a-button>
          <a-popconfirm title="确认删除该线路？" @confirm="$emit('remove', record)">
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
  { title: '编码', dataIndex: 'code', width: 160 },
  { title: '线路名称', dataIndex: 'name', width: 180 },
  { title: '出发地', dataIndex: 'departureCity', width: 120 },
  { title: '目的地', dataIndex: 'destinationCity', width: 120 },
  { title: '行程', dataIndex: 'duration', width: 120 },
  { title: '分类', dataIndex: 'category', width: 120 },
  { title: '操作', dataIndex: 'actions', width: 180 }
];
</script>