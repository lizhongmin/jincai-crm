<template>
  <a-table :columns="columns" :data-source="items" row-key="id" :scroll="{ x: 1180 }">
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'customerType'">
        {{ mapCustomerType(record.customerType) }}
      </template>
      <template v-else-if="column.dataIndex === 'source'">
        {{ mapSource(record.source) }}
      </template>
      <template v-else-if="column.dataIndex === 'intentionLevel'">
        {{ mapIntention(record.intentionLevel) }}
      </template>
      <template v-else-if="column.dataIndex === 'status'">
        <a-tag :color="statusColor(record.status)">{{ mapStatus(record.status) }}</a-tag>
      </template>
      <template v-else-if="column.dataIndex === 'actions'">
        <a-space>
          <a-button type="link" @click="$emit('view', record)">详情</a-button>
          <a-button type="link" @click="$emit('edit', record)">编辑</a-button>
          <a-popconfirm title="确认删除该客户？" @confirm="$emit('remove', record)">
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
  { title: '客户名称', dataIndex: 'name', width: 150 },
  { title: '手机号', dataIndex: 'phone', width: 140 },
  { title: '客户类型', dataIndex: 'customerType', width: 110 },
  { title: '来源', dataIndex: 'source', width: 110 },
  { title: '意向等级', dataIndex: 'intentionLevel', width: 110 },
  { title: '状态', dataIndex: 'status', width: 110 },
  { title: '负责人', dataIndex: 'ownerUserName', width: 140, customRender: ({ text }: any) => text || '-' },
  { title: '城市', dataIndex: 'city', width: 120, customRender: ({ text }: any) => text || '-' },
  { title: '操作', dataIndex: 'actions', width: 180, fixed: 'right' }
];

const mapCustomerType = (value?: string) => ({ PERSONAL: '个人', ENTERPRISE: '企业' }[value || ''] || value || '-');
const mapSource = (value?: string) => ({
  MANUAL: '手工录入',
  REFERRAL: '老客推荐',
  ONLINE: '线上咨询',
  PHONE: '电话咨询',
  WALK_IN: '到店'
}[value || ''] || value || '-');
const mapIntention = (value?: string) => ({ HIGH: '高', MEDIUM: '中', LOW: '低' }[value || ''] || value || '-');
const mapStatus = (value?: string) => ({ ACTIVE: '正常', INACTIVE: '沉默', BLACKLIST: '黑名单' }[value || ''] || value || '-');
const statusColor = (value?: string) => ({ ACTIVE: 'green', INACTIVE: 'orange', BLACKLIST: 'red' }[value || ''] || 'default');
</script>
