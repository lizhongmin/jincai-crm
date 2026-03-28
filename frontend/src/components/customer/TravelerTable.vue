<template>
  <pro-table :columns="tableColumns" :data-source="items" row-key="id" :scroll="tableScroll">
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'actions'">
        <a-space>
          <a-button type="link" :disabled="!hasButtonPermission('BTN_TRAVELER_EDIT')" @click="$emit('edit', record)">编辑</a-button>
          <a-popconfirm title="确认删除该出行人？" @confirm="$emit('remove', record)">
            <a-button type="link" danger :disabled="!hasButtonPermission('BTN_TRAVELER_DELETE')">删除</a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </template>
  </pro-table>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import ProTable from '../common/ProTable.vue';
import { hasButtonPermission } from '../../utils/permission';

const props = withDefaults(defineProps<{ items: any[]; scrollX?: number | string | false }>(), {
  scrollX: 'max-content'
});
defineEmits<{ (event: 'edit', value: any): void; (event: 'remove', value: any): void }>();

const idTypeLabelMap: Record<string, string> = {
  ID_CARD: '身份证',
  PASSPORT: '护照',
  HK_MACAO_PASS: '港澳通行证',
  TAIWAN_PASS: '台胞证',
  MILITARY_ID: '军官证',
  OTHER: '其他'
};

const columns = [
  { title: '姓名', dataIndex: 'name', width: 130 },
  { title: '所属客户', dataIndex: 'customerName', width: 150, customRender: ({ text }: any) => text || '-' },
  { title: '证件类型', dataIndex: 'idType', width: 120, customRender: ({ text }: any) => idTypeLabelMap[text] || text || '-' },
  { title: '证件号', dataIndex: 'idNo', width: 220, customRender: ({ text }: any) => text || '-' },
  { title: '出生日期', dataIndex: 'birthday', width: 130, customRender: ({ text }: any) => text || '-' },
  {
    title: '年龄',
    dataIndex: 'age',
    width: 90,
    customRender: ({ record }: any) => {
      if (record.age === undefined || record.age === null || record.age === '') return '-';
      return `${record.age}岁`;
    }
  },
  { title: '民族', dataIndex: 'ethnicity', width: 120, customRender: ({ text }: any) => text || '-' },
  { title: '手机号', dataIndex: 'phone', width: 150, customRender: ({ text }: any) => text || '-' },
  { title: '操作', dataIndex: 'actions', width: 140, fixed: 'right' as const }
];

const tableColumns = computed(() => {
  if (props.scrollX === false) {
    return columns.map((column) => {
      if (column.dataIndex === 'actions') {
        return { ...column, fixed: undefined };
      }
      return column;
    });
  }
  return columns;
});

const tableScroll = computed(() => {
  if (props.scrollX === false) {
    return undefined;
  }
  return { x: props.scrollX };
});
</script>
