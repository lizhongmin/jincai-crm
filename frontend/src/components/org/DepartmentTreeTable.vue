<template>
  <a-table
    :columns="columns"
    :data-source="items"
    row-key="id"
    :pagination="false"
    :default-expand-all-rows="true"
    children-column-name="children"
  >
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'canDelete'">
        <a-tag :color="record.hasUsers ? 'orange' : record.hasChildren ? 'blue' : 'green'">
          {{ record.hasUsers ? '有用户' : record.hasChildren ? '有子部门' : '可删除' }}
        </a-tag>
      </template>
      <template v-if="column.dataIndex === 'actions'">
        <a-space>
          <a-button type="link" @click="$emit('edit', record)">编辑</a-button>
          <a-popconfirm
            v-if="!record.hasChildren && !record.hasUsers"
            title="确认删除该部门？"
            @confirm="$emit('remove', record)"
          >
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
  { title: '部门名称', dataIndex: 'name' },
  { title: '上级部门', dataIndex: 'parentName', customRender: ({ text }: any) => text || '-' },
  { title: '部门负责人', dataIndex: 'leaderName', customRender: ({ text }: any) => text || '-' },
  {
    title: '层级',
    dataIndex: 'treePath',
    width: 100,
    customRender: ({ record }: any) => (String(record.treePath || '/').split('/').filter(Boolean).length + 1)
  },
  { title: '删除条件', dataIndex: 'canDelete', width: 120 },
  { title: '操作', dataIndex: 'actions', width: 140 }
];
</script>