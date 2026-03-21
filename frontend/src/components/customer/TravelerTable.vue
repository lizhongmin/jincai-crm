<template>
  <a-table :columns="columns" :data-source="items" row-key="id">
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'actions'">
        <a-space>
          <a-button type="link" @click="$emit('edit', record)">编辑</a-button>
          <a-popconfirm title="确认删除该出行人？" @confirm="$emit('remove', record)">
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
  { title: '姓名', dataIndex: 'name', width: 120 },
  { title: '所属客户', dataIndex: 'customerName', width: 140, customRender: ({ text }: any) => text || '-' },
  { title: '证件类型', dataIndex: 'idType', width: 120 },
  { title: '证件号', dataIndex: 'idNo' },
  { title: '手机号', dataIndex: 'phone', width: 140 },
  { title: '操作', dataIndex: 'actions', width: 140 }
];
</script>
