<template>
  <pro-table :columns="columns" :data-source="items" row-key="id" :pagination="false" :scroll="{ x: 'max-content' }">
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'actions' && editable">
        <a-space>
          <a-button type="link" @click="$emit('edit', record)">编辑</a-button>
          <a-popconfirm title="确认删除该价格项？" @confirm="$emit('remove', record)">
            <a-button type="link" danger>删除</a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </template>
  </pro-table>
</template>

<script setup lang="ts">
import ProTable from '../common/ProTable.vue';
import { CURRENCY_LABEL_MAP, PRICE_TYPE_LABEL_MAP, enumLabel } from '../../constants/display';

const props = withDefaults(defineProps<{ items: any[]; editable?: boolean }>(), {
  editable: true
});
defineEmits<{ (event: 'edit', value: any): void; (event: 'remove', value: any): void }>();

const columns = [
  { title: '价格类型', dataIndex: 'priceType', width: 140, customRender: ({ text }: any) => enumLabel(PRICE_TYPE_LABEL_MAP, text) },
  { title: '显示名称', dataIndex: 'priceLabel', width: 180, customRender: ({ text }: any) => text || '-' },
  { title: '价格', dataIndex: 'price', width: 120 },
  { title: '币种', dataIndex: 'currency', width: 120, customRender: ({ text }: any) => enumLabel(CURRENCY_LABEL_MAP, text) },
  { title: '说明', dataIndex: 'description', customRender: ({ text }: any) => text || '-' },
  ...(props.editable ? [{ title: '操作', dataIndex: 'actions', width: 140, fixed: 'right' as const }] : [])
];
</script>
