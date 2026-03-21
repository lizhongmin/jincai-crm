<template>
  <a-table :columns="columns" :data-source="items" row-key="template.id">
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'active'">
        <a-tag :color="record.template.active ? 'green' : 'default'">
          {{ record.template.active ? '启用' : '停用' }}
        </a-tag>
      </template>
      <template v-else-if="column.dataIndex === 'nodes'">
        {{ (record.nodes || []).map((item: any) => `${item.stepOrder}. ${item.nodeName}`).join(' / ') }}
      </template>
      <template v-else-if="column.dataIndex === 'actions'">
        <a-space>
          <a-button type="link" size="small" @click="emit('edit', record)">编辑</a-button>
          <a-popconfirm title="确认删除该模板？" @confirm="emit('remove', record.template.id)">
            <a-button type="link" danger size="small">删除</a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </template>
  </a-table>
</template>

<script setup lang="ts">
const props = defineProps<{ items: any[] }>();

const emit = defineEmits<{
  (e: 'edit', record: any): void;
  (e: 'remove', id: number): void;
}>();

const columns = [
  { title: '模板名称', dataIndex: ['template', 'name'] },
  { title: '订单类型', dataIndex: ['template', 'orderType'], width: 110 },
  { title: '产品分类', dataIndex: ['template', 'productCategory'], width: 120 },
  { title: '金额范围', dataIndex: 'amount', width: 180 },
  { title: '状态', dataIndex: 'active', width: 90 },
  { title: '审批节点', dataIndex: 'nodes' },
  { title: '操作', dataIndex: 'actions', width: 130 }
];
</script>