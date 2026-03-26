<template>
  <pro-table
    class="org-user-table"
    :columns="columns"
    :data-source="props.items"
    :pagination="paginationConfig"
    :loading="props.loading"
    row-key="id"
    size="middle"
    :scroll="{ x: 'max-content' }"
    @change="handleTableChange"
  >
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'enabled'">
        <a-switch
          size="small"
          :checked="Boolean(record.enabled)"
          :disabled="isAdmin(record)"
          @change="$emit('toggle-status', record)"
        />
      </template>

      <template v-if="column.dataIndex === 'gender'">
        {{ genderLabel(record.gender) }}
      </template>

      <template v-if="column.dataIndex === 'email'">
        <a-tooltip :title="record.email || '-'">
          <span class="ellipsis">{{ record.email || '-' }}</span>
        </a-tooltip>
      </template>

      <template v-if="column.dataIndex === 'roleNames'">
        <a-space size="4" wrap>
          <a-tag v-for="role in (record.roleNames || []).slice(0, 2)" :key="role" class="role-tag">{{ role }}</a-tag>
          <a-tag v-if="(record.roleNames || []).length > 2" class="role-tag">+{{ (record.roleNames || []).length - 2 }}</a-tag>
          <span v-if="!(record.roleNames || []).length">-</span>
        </a-space>
      </template>

      <template v-if="column.dataIndex === 'actions'">
        <a-space size="small" wrap>
          <a-button type="link" size="small" :disabled="isAdmin(record)" @click="$emit('edit', record)">编辑</a-button>
          <a-button type="link" size="small" :disabled="isAdmin(record)" @click="$emit('reset-password', record)">重置密码</a-button>
          <a-popconfirm v-if="!isAdmin(record)" title="确认删除该用户？" @confirm="$emit('remove', record)">
            <a-button type="link" danger size="small">删除</a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </template>
  </pro-table>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import ProTable from '../common/ProTable.vue';

const props = withDefaults(defineProps<{ items: any[]; loading?: boolean; pagination?: any }>(), {
  loading: false,
  pagination: undefined
});

const emit = defineEmits<{
  (event: 'edit', value: any): void;
  (event: 'toggle-status', value: any): void;
  (event: 'reset-password', value: any): void;
  (event: 'remove', value: any): void;
  (event: 'page-change', value: { current?: number; pageSize?: number }): void;
}>();

const columns = [
  { title: '姓名', dataIndex: 'fullName', width: 130 },
  { title: '状态', dataIndex: 'enabled', width: 90 },
  { title: '性别', dataIndex: 'gender', width: 80 },
  { title: '手机号', dataIndex: 'phone', width: 150 },
  { title: '邮箱', dataIndex: 'email', width: 200 },
  { title: '部门', dataIndex: 'departmentName', width: 120 },
  { title: '角色', dataIndex: 'roleNames', width: 180 },
  { title: '操作', dataIndex: 'actions', width: 220, fixed: 'right' as const }
];

const defaultPaginationConfig = {
  pageSize: 10,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
};

const paginationConfig = computed(() => props.pagination || defaultPaginationConfig);

const handleTableChange = (pagination: { current?: number; pageSize?: number }) => {
  emit('page-change', pagination);
};

const genderLabel = (gender?: string) => {
  const map: Record<string, string> = {
    MALE: '男',
    FEMALE: '女',
    UNKNOWN: '-'
  };
  return map[gender || ''] || '-';
};

const isAdmin = (record: any) => String(record.username || '').toLowerCase() === 'admin';
</script>

<style scoped>
.org-user-table {
  padding: 8px 12px 12px;
}

.org-user-table :deep(.ant-table) {
  table-layout: fixed;
}

.org-user-table :deep(.ant-table-thead > tr > th) {
  background: #fafcff;
  color: #2c3a50;
  font-weight: 600;
  border-bottom: 1px solid #e8edf5;
}

.org-user-table :deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid #eff3f8;
  vertical-align: middle;
}

.org-user-table :deep(.ant-table-tbody > tr:hover > td) {
  background: #f7fbff;
}

.ellipsis {
  display: inline-block;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.role-tag {
  margin-inline-end: 0;
  border-radius: 10px;
  border-color: #d9ecff;
  background: #eef6ff;
  color: #2c6fd7;
}
</style>
