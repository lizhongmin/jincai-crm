<template>
  <a-table :columns="columns" :data-source="items" row-key="id" :pagination="false">
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'actions'">
        <a-space>
          <a-button type="link" @click="$emit('edit', record)">编辑</a-button>
          <a-popconfirm title="确认删除该价格项？" @confirm="$emit('remove', record)">
            <a-button type="link" danger>删除</a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </template>
  </a-table>
</template>

<script setup lang="ts">
defineProps<{ items: any[] }>();
defineEmits<{ (event: 'edit', value: any): void; (event: 'remove', value: any): void }>();

const columns = [
  { title: '价格类型', dataIndex: 'priceType', width: 140 },
  { title: '显示名称', dataIndex: 'priceLabel', width: 180 },
  { title: '价格', dataIndex: 'price', width: 120 },
  { title: '币种', dataIndex: 'currency', width: 100 },
  { title: '说明', dataIndex: 'description' },
  { title: '操作', dataIndex: 'actions', width: 140 }
];
</script>