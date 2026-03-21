<template>
  <a-table :columns="columns" :data-source="items" row-key="id">
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'dataScope'">
        {{ dataScopeLabel(record.dataScope) }}
      </template>
      <template v-if="column.dataIndex === 'enabled'">
        <a-tag :color="record.enabled ? 'green' : 'red'">{{ record.enabled ? '启用' : '禁用' }}</a-tag>
      </template>
      <template v-if="column.dataIndex === 'actions'">
        <a-space>
          <a-button type="link" :disabled="isAdmin(record)" @click="$emit('edit', record)">编辑</a-button>
          <a-button type="link" :disabled="isAdmin(record)" @click="$emit('toggle-status', record)">
            {{ record.enabled ? '禁用' : '启用' }}
          </a-button>
          <a-button type="link" :disabled="isAdmin(record)" @click="$emit('reset-password', record)">重置密码</a-button>
          <a-popconfirm v-if="!isAdmin(record)" title="确认删除该用户？" @confirm="$emit('remove', record)">
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
  (event: 'edit', value: any): void;
  (event: 'toggle-status', value: any): void;
  (event: 'reset-password', value: any): void;
  (event: 'remove', value: any): void;
}>();

const columns = [
  { title: '账号', dataIndex: 'username', width: 140 },
  { title: '姓名', dataIndex: 'fullName', width: 120 },
  { title: '工号', dataIndex: 'employeeNo', width: 120 },
  { title: '手机号', dataIndex: 'phone', width: 140 },
  { title: '邮箱', dataIndex: 'email', width: 180 },
  { title: '部门路径', dataIndex: 'departmentPath', customRender: ({ text }: any) => text || '-' },
  {
    title: '角色',
    dataIndex: 'roleNames',
    customRender: ({ record }: any) => (record.roleNames || []).join('、') || '-'
  },
  { title: '数据范围', dataIndex: 'dataScope', width: 140 },
  { title: '状态', dataIndex: 'enabled', width: 100 },
  { title: '操作', dataIndex: 'actions', width: 300 }
];

const dataScopeLabel = (scope?: string) => {
  const map: Record<string, string> = {
    SELF: '仅本人',
    DEPARTMENT: '本部门',
    DEPARTMENT_TREE: '本部门及子部门',
    ALL: '全公司'
  };
  return map[scope || ''] || scope || '-';
};

const isAdmin = (record: any) => String(record.username || '').toLowerCase() === 'admin';
</script>