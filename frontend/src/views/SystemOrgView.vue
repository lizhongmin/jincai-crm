<template>
  <div class="org-page">
    <div class="biz-summary">
      <div class="item">
        <span class="label">部门数量</span>
        <strong class="value">{{ allDepartmentNodes.length }}</strong>
      </div>
      <div class="item">
        <span class="label">用户数量</span>
        <strong class="value">{{ userTotal }}</strong>
      </div>
      <div class="item">
        <span class="label">启用账号</span>
        <strong class="value">{{ enabledUserCount }}</strong>
      </div>
      <div class="item">
        <span class="label">角色数量</span>
        <strong class="value">{{ roles.length }}</strong>
      </div>
    </div>

    <a-card class="section-card" :bordered="false">
      <div class="org-workspace">
        <section class="dept-pane">
          <div class="pane-toolbar">
            <a-input
              v-model:value="departmentKeyword"
              allow-clear
              placeholder="通过部门名称搜索"
            />
            <a-button @click="loadDepartments">
              <template #icon><reload-outlined /></template>
            </a-button>
          </div>

          <div class="dept-all" :class="{ active: !selectedDepartmentId }" @click="selectDepartment()">
            全部部门
          </div>

          <div class="dept-tree-wrap">
            <a-tree
              :tree-data="departmentTree"
              :field-names="{ children: 'children', title: 'name', key: 'id' }"
              :selected-keys="selectedDepartmentId ? [selectedDepartmentId] : []"
              :expanded-keys="expandedDepartmentKeys"
              :auto-expand-parent="false"
              @expand="onDepartmentExpand"
            >
              <template #title="nodeData">
                <div class="dept-node" @click="selectDepartment(nodeData.id)">
                  <span class="dept-node-name">{{ nodeData.name }}</span>
                  <a-space class="dept-node-actions" size="small">
                    <a-button type="text" size="small" @click.stop="openDepartment({ id: nodeData.id, name: nodeData.name, parentId: nodeData.parentId, sort: nodeData.sort })">
                      <template #icon><edit-outlined /></template>
                    </a-button>
                    <a-button type="text" size="small" @click.stop="openAddChild(nodeData)">
                      <template #icon><plus-outlined /></template>
                    </a-button>
                    <a-popconfirm
                      v-if="nodeData.parentId != null"
                      title="确认删除该部门？"
                      @confirm="removeDepartment(nodeData.id)"
                    >
                      <a-button type="text" size="small" danger @click.stop>
                        <template #icon><delete-outlined /></template>
                      </a-button>
                    </a-popconfirm>
                  </a-space>
                </div>
              </template>
            </a-tree>
          </div>
        </section>

        <section class="user-pane">
          <div class="user-toolbar">
            <div class="toolbar-row">
              <a-button type="primary" @click="openUser()">
                <template #icon><user-add-outlined /></template>
                新增用户
              </a-button>
            </div>

            <div class="toolbar-row">
              <a-input-search
                v-model:value="userKeyword"
                placeholder="通过姓名/手机号搜索"
                style="width: 260px"
                @search="loadUsers"
              />
              <a-select v-model:value="userFilter" style="width: 120px">
                <a-select-option value="all">全部用户</a-select-option>
                <a-select-option value="enabled">启用账号</a-select-option>
                <a-select-option value="disabled">禁用账号</a-select-option>
              </a-select>
            </div>
          </div>

          <user-list-table
            :loading="userLoading"
            :data-source="users"
            :pagination="{
              current: userPage,
              pageSize: userPageSize,
              total: userTotal,
              showSizeChanger: true,
              showTotal: (total: number) => `共 ${total} 人`
            }"
            @change="onUserPageChange"
            @edit="openUser"
            @delete="removeUser"
            @reset-password="resetUserPassword"
            @enable="enableUser"
            @disable="disableUser"
            @refresh="loadUsers"
          />
        </section>
      </div>
    </a-card>

    <a-modal
      v-model:open="departmentModal"
      :title="departmentForm.id ? '编辑部门' : '新增部门'"
      @ok="saveDepartment"
    >
      <a-form layout="vertical">
        <a-form-item label="部门名称" required>
          <a-input v-model:value="departmentForm.name" placeholder="请输入部门名称" />
        </a-form-item>
        <a-form-item label="上级部门">
          <a-input v-model:value="departmentForm.parentName" disabled placeholder="无" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="departmentForm.sort" :min="0" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-drawer
      v-model:open="userModal"
      :title="userForm.id ? '编辑用户' : '新增用户'"
      :width="480"
      placement="right"
    >
      <template #extra>
        <a-space>
          <a-button @click="userModal = false">取消</a-button>
          <a-button type="primary" @click="saveUser">确定</a-button>
        </a-space>
      </template>

      <a-form layout="vertical">
        <a-form-item label="姓名" required>
          <a-input v-model:value="userForm.fullName" placeholder="请输入姓名" />
        </a-form-item>
        <a-form-item label="手机号" required>
          <a-input v-model:value="userForm.phone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item v-if="!userForm.id" label="登录账号" required>
          <a-input v-model:value="userForm.username" placeholder="请输入登录账号" />
        </a-form-item>
        <a-form-item label="所属部门">
          <a-tree-select
            v-model:value="userForm.departmentId"
            :tree-data="departmentOptions"
            placeholder="请选择所属部门"
            :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
            :field-names="{ children: 'children', label: 'name', value: 'id' }"
            tree-default-expand-all
            allow-clear
          />
        </a-form-item>
        <a-form-item label="分配角色">
          <a-select
            v-model:value="userForm.roleIds"
            mode="multiple"
            placeholder="请选择角色"
            style="width: 100%"
          >
            <a-select-option v-for="role in roles" :key="role.id" :value="role.id">
              {{ role.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="数据权限范围">
          <a-select v-model:value="userForm.dataScope" placeholder="请选择数据权限范围">
            <a-select-option value="SELF">仅本人</a-select-option>
            <a-select-option value="DEPARTMENT">本部门</a-select-option>
            <a-select-option value="DEPARTMENT_TREE">本部门及子部门</a-select-option>
            <a-select-option value="ALL">全部数据</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import {
  DeleteOutlined,
  EditOutlined,
  PlusOutlined,
  ReloadOutlined,
  UserAddOutlined
} from '@ant-design/icons-vue';
import { computed, onMounted, reactive, ref, watch } from 'vue';
import UserListTable from '../components/org/UserListTable.vue';
import { orgApi } from '../api/crm';
import { notifyError, notifySuccess } from '../utils/notify';

const departmentModal = ref(false);
const userModal = ref(false);

const departmentTree = ref<any[]>([]);
const users = ref<any[]>([]);
const roles = ref<any[]>([]);

const departmentKeyword = ref('');
const userKeyword = ref('');
const userFilter = ref('all');

const userLoading = ref(false);
const userPage = ref(1);
const userPageSize = ref(10);
const userTotal = ref(0);

const expandedDepartmentKeys = ref<number[]>([]);

const selectedDepartmentId = ref<number>();

const departmentForm = reactive({
  id: undefined as number | undefined,
  name: '',
  parentId: undefined as number | undefined,
  parentName: '',
  sort: 0
});

const userForm = reactive({
  id: undefined as number | undefined,
  username: '',
  fullName: '',
  phone: '',
  departmentId: undefined as number | undefined,
  roleIds: [] as number[],
  dataScope: 'SELF'
});

const allDepartmentNodes = computed(() => {
  const output: any[] = [];
  const walk = (nodes: any[], prefix: string) => {
    for (const node of nodes || []) {
      output.push({ ...node, name: prefix + node.name });
      walk(node.children, prefix + '　');
    }
  };
  walk(departmentTree.value || [], '');
  return output;
});

const departmentOptions = computed(() => allDepartmentNodes.value);
const enabledUserCount = computed(() => users.value.filter((item) => item.enabled).length);

const findDepartmentName = (id: number | undefined): string => {
  if (id == null) return '';
  const find = (nodes: any[]): string => {
    for (const node of nodes || []) {
      if (node.id === id) return node.name;
      const found = find(node.children || []);
      if (found) return found;
    }
    return '';
  };
  return find(departmentTree.value);
};

function mapTreeWithKey(nodes: any[]): any[] {
  return (nodes || []).map((node) => ({
    ...node,
    key: node.id,
    children: node.children?.length ? mapTreeWithKey(node.children) : undefined
  }));
}

const collectDescendantIds = (rootId: number): number[] => {
  const output: number[] = [];
  const walk = (nodes: any[]) => {
    for (const node of nodes || []) {
      if (node.id === rootId) {
        continue;
      }
      output.push(node.id);
      walk(node.children);
    }
  };
  walk(departmentTree.value);
  return output;
};

const loadDepartments = async () => {
  try {
    const { data } = await orgApi.departmentsTree();
    departmentTree.value = mapTreeWithKey(data.data || []);
    expandedDepartmentKeys.value = (data.data || []).map((item: any) => item.id);
  } catch (error) {
    notifyError(error);
  }
};

const loadUsers = async () => {
  userLoading.value = true;
  try {
    const params: any = {
      page: userPage.value,
      size: userPageSize.value,
      keyword: userKeyword.value.trim()
    };

    if (selectedDepartmentId.value) {
      params.departmentId = selectedDepartmentId.value;
    }

    if (userFilter.value === 'enabled') {
      params.enabled = true;
    } else if (userFilter.value === 'disabled') {
      params.enabled = false;
    }

    const { data } = await orgApi.usersPage(params);
    users.value = data.data?.items || [];
    userTotal.value = Number(data.data?.total || 0);
  } catch (error) {
    notifyError(error);
  } finally {
    userLoading.value = false;
  }
};

const loadRoles = async () => {
  try {
    const { data } = await orgApi.roles();
    roles.value = data.data || [];
  } catch (error) {
    notifyError(error);
  }
};

const load = async () => {
  await Promise.all([loadDepartments(), loadRoles(), loadUsers()]);
};

const selectDepartment = (id?: number) => {
  selectedDepartmentId.value = id;
  userPage.value = 1;
  loadUsers();
};

const onDepartmentExpand = (keys: number[]) => {
  expandedDepartmentKeys.value = keys;
};

const openDepartment = (record?: any) => {
  departmentForm.id = record?.id;
  departmentForm.name = record?.name || '';
  departmentForm.parentId = record?.parentId;
  departmentForm.parentName = findDepartmentName(record?.parentId) || '无（顶级部门）';
  departmentForm.sort = record?.sort || 0;
  departmentModal.value = true;
};

const openAddChild = (parentRecord: any) => {
  departmentForm.id = undefined;
  departmentForm.name = '';
  departmentForm.parentId = parentRecord.id;
  departmentForm.parentName = parentRecord.name || '';
  departmentForm.sort = 0;
  departmentModal.value = true;
};

const saveDepartment = async () => {
  if (!departmentForm.name.trim()) {
    return;
  }

  try {
    const payload = {
      name: departmentForm.name,
      parentId: departmentForm.parentId,
      sort: departmentForm.sort
    };

    if (departmentForm.id) {
      await orgApi.updateDepartment(departmentForm.id, payload);
      notifySuccess('部门更新成功');
    } else {
      await orgApi.createDepartment(payload);
      notifySuccess('部门创建成功');
    }

    departmentModal.value = false;
    await loadDepartments();
  } catch (error) {
    notifyError(error);
  }
};

const removeDepartment = async (id: number) => {
  try {
    await orgApi.deleteDepartment(id);
    notifySuccess('部门删除成功');
    await loadDepartments();
  } catch (error) {
    notifyError(error);
  }
};

const openUser = (record?: any) => {
  userForm.id = record?.id;
  userForm.username = record?.username || '';
  userForm.fullName = record?.fullName || '';
  userForm.phone = record?.phone || '';
  userForm.departmentId = record?.departmentId;
  userForm.roleIds = (record?.roles || []).map((r: any) => r.id);
  userForm.dataScope = record?.dataScope || 'SELF';
  userModal.value = true;
};

const saveUser = async () => {
  if (!userForm.fullName.trim() || !userForm.phone.trim() || (!userForm.id && !userForm.username.trim())) {
    return;
  }

  try {
    const payload: any = {
      fullName: userForm.fullName,
      phone: userForm.phone,
      departmentId: userForm.departmentId,
      roleIds: userForm.roleIds,
      dataScope: userForm.dataScope
    };

    if (userForm.id) {
      await orgApi.updateUser(userForm.id, payload);
      notifySuccess('用户更新成功');
    } else {
      payload.username = userForm.username;
      await orgApi.createUser(payload);
      notifySuccess('用户创建成功');
    }

    userModal.value = false;
    await loadUsers();
  } catch (error) {
    notifyError(error);
  }
};

const removeUser = async (record: any) => {
  try {
    await orgApi.deleteUser(record.id);
    notifySuccess('用户删除成功');
    await loadUsers();
  } catch (error) {
    notifyError(error);
  }
};

const resetUserPassword = async (record: any) => {
  try {
    await orgApi.resetUserPassword(record.id);
    notifySuccess('密码重置成功');
  } catch (error) {
    notifyError(error);
  }
};

const enableUser = async (record: any) => {
  try {
    await orgApi.updateUserStatus(record.id,true);
    notifySuccess('用户已启用');
    await loadUsers();
  } catch (error) {
    notifyError(error);
  }
};

const disableUser = async (record: any) => {
  try {
    await orgApi.updateUserStatus(record.id,false);
    notifySuccess('用户已禁用');
    await loadUsers();
  } catch (error) {
    notifyError(error);
  }
};

const onUserPageChange = (pagination: { current?: number; pageSize?: number }) => {
  userPage.value = pagination.current || 1;
  userPageSize.value = pagination.pageSize || 10;
  loadUsers();
};

watch(departmentKeyword, () => {
  // 可以添加前端过滤逻辑
});

onMounted(load);
</script>

<style scoped>
.org-page {
  display: grid;
  gap: 10px;
}

.biz-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.org-workspace {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 12px;
  min-height: calc(100vh - 166px);
}

.dept-pane {
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fff;
  display: grid;
  grid-template-rows: auto auto minmax(0, 1fr);
}

.pane-toolbar {
  display: flex;
  gap: 8px;
  padding: 10px;
  border-bottom: 1px solid var(--line);
}

.dept-all {
  margin: 8px 10px;
  padding: 6px 12px;
  border-radius: 6px;
  color: #4a5872;
  cursor: pointer;
}

.dept-all.active {
  background: #eaf4ff;
  color: #1677ff;
  font-weight: 600;
}

.dept-tree-wrap {
  padding: 8px 10px 12px;
  max-height: calc(100vh - 310px);
  overflow: auto;
}

.dept-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
}

.dept-node-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dept-node-actions {
  opacity: 0;
  transition: opacity 0.2s ease;
}

:deep(.ant-tree-node-content-wrapper:hover) .dept-node-actions,
:deep(.ant-tree-node-selected) .dept-node-actions {
  opacity: 1;
}

.user-pane {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 12px;
}

.user-toolbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

@media (max-width: 1200px) {
  .org-workspace {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .user-search {
    width: 220px;
  }
}
</style>
