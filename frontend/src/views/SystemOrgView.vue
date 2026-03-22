<template>
  <div class="org-page">
    <div class="biz-summary">
      <div class="item">
        <span class="label">部门数量</span>
        <strong class="value">{{ allDepartmentNodes.length }}</strong>
      </div>
      <div class="item">
        <span class="label">用户数量</span>
        <strong class="value">{{ users.length }}</strong>
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
            >
              <template #prefix>
                <search-outlined />
              </template>
            </a-input>
            <a-tooltip title="新增部门">
              <a-button @click="openDepartment()">
                <template #icon><plus-outlined /></template>
              </a-button>
            </a-tooltip>
            <a-tooltip title="刷新">
              <a-button @click="load">
                <template #icon><reload-outlined /></template>
              </a-button>
            </a-tooltip>
          </div>

          <div class="dept-all" :class="{ active: !selectedDepartmentId }" @click="clearDepartmentFilter">
            全部部门
          </div>

          <div class="dept-tree-wrap">
            <a-tree
              block-node
              :selectedKeys="selectedDeptKeys"
              :tree-data="filteredDepartmentTree"
              :defaultExpandAll="true"
              @select="onDepartmentSelect"
            >
              <template #title="{ dataRef }">
                <div class="dept-node">
                  <span class="dept-node-name">{{ dataRef.name }}</span>
                  <a-space class="dept-node-actions" size="small" @click.stop>
                    <a-tooltip title="新增子部门">
                      <a-button type="text" size="small" @click.stop="openDepartmentWithParent(dataRef.id)">
                        <template #icon><plus-outlined /></template>
                      </a-button>
                    </a-tooltip>
                    <a-tooltip title="编辑部门">
                      <a-button type="text" size="small" @click.stop="openDepartment(dataRef)">
                        <template #icon><edit-outlined /></template>
                      </a-button>
                    </a-tooltip>
                    <a-popconfirm
                      v-if="!dataRef.hasChildren && !dataRef.hasUsers"
                      title="确认删除该部门？"
                      @confirm="removeDepartment(dataRef)"
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
          <div class="pane-toolbar user-toolbar">
            <a-space>
              <a-button type="primary" @click="openUser()">
                <template #icon><user-add-outlined /></template>
                添加成员
              </a-button>
            </a-space>

            <a-space>
              <a-input
                v-model:value="userKeyword"
                allow-clear
                class="user-search"
                placeholder="通过姓名/手机号搜索"
              >
                <template #prefix>
                  <search-outlined />
                </template>
              </a-input>
              <a-button @click="load">
                <template #icon><reload-outlined /></template>
              </a-button>
            </a-space>
          </div>

          <user-list-table
            :items="visibleUsers"
            @edit="openUser"
            @toggle-status="toggleUserStatus"
            @reset-password="resetPassword"
            @remove="removeUser"
          />
        </section>
      </div>
    </a-card>

    <a-drawer v-model:open="departmentModal" :title="departmentForm.id ? '编辑部门' : '新增部门'" placement="right" :width="520">
      <template #extra>
        <a-space>
          <a-button @click="departmentModal = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="saveDepartment">保存</a-button>
        </a-space>
      </template>
      <a-form layout="vertical">
        <a-form-item label="部门名称" required>
          <a-input v-model:value="departmentForm.name" placeholder="如：华东销售一部" />
        </a-form-item>
        <a-form-item label="上级部门">
          <a-select v-model:value="departmentForm.parentId" allow-clear placeholder="不选则为一级部门">
            <a-select-option v-for="item in parentDepartmentOptions" :key="item.id" :value="item.id">{{ item.pathName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="部门负责人">
          <a-select v-model:value="departmentForm.leaderUserId" allow-clear placeholder="请选择部门负责人">
            <a-select-option v-for="item in users" :key="item.id" :value="item.id">{{ item.fullName }} ({{ item.username }})</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-drawer>

    <a-drawer v-model:open="userModal" :title="userForm.id ? '编辑用户' : '新增用户'" placement="right" :width="760">
      <template #extra>
        <a-space>
          <a-button @click="userModal = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="saveUser">保存</a-button>
        </a-space>
      </template>
      <a-form layout="vertical">
        <a-form-item label="账号" required>
          <a-input v-model:value="userForm.username" :disabled="Boolean(userForm.id)" placeholder="如：zhangsan" />
        </a-form-item>
        <a-form-item label="姓名" required>
          <a-input v-model:value="userForm.fullName" placeholder="如：张三" />
        </a-form-item>
        <a-form-item label="手机号" required>
          <a-input v-model:value="userForm.phone" placeholder="11位手机号" />
        </a-form-item>
        <a-form-item label="工号">
          <a-input v-model:value="userForm.employeeNo" placeholder="如：EMP1001" />
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input v-model:value="userForm.email" placeholder="name@example.com" />
        </a-form-item>
        <a-form-item label="性别">
          <a-select v-model:value="userForm.gender" allow-clear>
            <a-select-option value="MALE">男</a-select-option>
            <a-select-option value="FEMALE">女</a-select-option>
            <a-select-option value="UNKNOWN">未知</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="岗位">
          <a-input v-model:value="userForm.title" placeholder="如：销售顾问" />
        </a-form-item>
        <a-form-item label="入职日期">
          <a-input v-model:value="userForm.hireDate" placeholder="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item label="紧急联系电话">
          <a-input v-model:value="userForm.emergencyPhone" />
        </a-form-item>
        <a-form-item label="部门" required>
          <a-select v-model:value="userForm.departmentId" placeholder="请选择部门">
            <a-select-option v-for="item in departmentOptions" :key="item.id" :value="item.id">{{ item.pathName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="数据范围" required>
          <a-select v-model:value="userForm.dataScope">
            <a-select-option value="SELF">仅本人</a-select-option>
            <a-select-option value="DEPARTMENT">本部门</a-select-option>
            <a-select-option value="DEPARTMENT_TREE">本部门及子部门</a-select-option>
            <a-select-option value="ALL">全公司</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model:value="userForm.roleIds" mode="multiple" placeholder="可多选角色">
            <a-select-option v-for="item in roles" :key="item.id" :value="item.id">{{ item.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="账号状态" required>
          <a-switch v-model:checked="userForm.enabled" checked-children="启用" un-checked-children="禁用" />
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
  SearchOutlined,
  UserAddOutlined
} from '@ant-design/icons-vue';
import { computed, onMounted, reactive, ref } from 'vue';
import UserListTable from '../components/org/UserListTable.vue';
import { orgApi } from '../api/crm';
import { notifyError, notifySuccess } from '../utils/notify';

const saving = ref(false);
const departmentModal = ref(false);
const userModal = ref(false);

const departmentTree = ref<any[]>([]);
const users = ref<any[]>([]);
const roles = ref<any[]>([]);

const departmentKeyword = ref('');
const userKeyword = ref('');
const selectedDepartmentId = ref<number>();

const departmentForm = reactive({
  id: undefined as number | undefined,
  name: '',
  parentId: undefined as number | undefined,
  leaderUserId: undefined as number | undefined
});

const userForm = reactive({
  id: undefined as number | undefined,
  username: '',
  fullName: '',
  phone: '',
  employeeNo: '',
  email: '',
  gender: undefined as string | undefined,
  title: '',
  hireDate: '',
  emergencyPhone: '',
  departmentId: undefined as number | undefined,
  dataScope: 'SELF',
  enabled: true,
  roleIds: [] as number[]
});

const treeDataForPanel = computed(() => mapTreeWithKey(departmentTree.value || []));

const filteredDepartmentTree = computed(() => {
  const keyword = departmentKeyword.value.trim().toLowerCase();
  if (!keyword) {
    return treeDataForPanel.value;
  }
  return filterTree(treeDataForPanel.value, keyword);
});

const selectedDeptKeys = computed(() => (selectedDepartmentId.value ? [String(selectedDepartmentId.value)] : []));

const allDepartmentNodes = computed(() => {
  const output: any[] = [];
  const walk = (nodes: any[], parentPath: string) => {
    for (const node of nodes || []) {
      const pathName = parentPath ? `${parentPath} / ${node.name}` : node.name;
      output.push({ ...node, pathName });
      walk(node.children || [], pathName);
    }
  };
  walk(departmentTree.value || [], '');
  return output;
});

const departmentOptions = computed(() => allDepartmentNodes.value);
const enabledUserCount = computed(() => users.value.filter((item) => item.enabled).length);

const parentDepartmentOptions = computed(() => {
  if (!departmentForm.id) {
    return allDepartmentNodes.value;
  }
  const blocked = new Set<number>([departmentForm.id, ...collectDescendantIds(departmentForm.id)]);
  return allDepartmentNodes.value.filter((item) => !blocked.has(item.id));
});

const visibleUsers = computed(() => {
  let result = [...(users.value || [])];

  if (selectedDepartmentId.value) {
    const allowedIds = new Set<number>([selectedDepartmentId.value, ...collectDescendantIds(selectedDepartmentId.value)]);
    result = result.filter((item) => item.departmentId && allowedIds.has(item.departmentId));
  }

  const keyword = userKeyword.value.trim().toLowerCase();
  if (keyword) {
    result = result.filter((item) =>
      [item.fullName, item.username, item.phone, item.email]
        .filter(Boolean)
        .some((field) => String(field).toLowerCase().includes(keyword))
    );
  }

  return result;
});

function mapTreeWithKey(nodes: any[]): any[] {
  return (nodes || []).map((node) => ({
    ...node,
    key: String(node.id),
    children: mapTreeWithKey(node.children || [])
  }));
}

function filterTree(nodes: any[], keyword: string): any[] {
  const filtered: any[] = [];
  for (const node of nodes || []) {
    const children = filterTree(node.children || [], keyword);
    const selfMatched = String(node.name || '').toLowerCase().includes(keyword);
    if (selfMatched || children.length) {
      filtered.push({ ...node, children });
    }
  }
  return filtered;
}

function findTreeNodeById(nodes: any[], id: number): any | null {
  for (const node of nodes || []) {
    if (node.id === id) {
      return node;
    }
    const child = findTreeNodeById(node.children || [], id);
    if (child) {
      return child;
    }
  }
  return null;
}

function collectAllChildren(nodes: any[], output: number[]) {
  for (const node of nodes || []) {
    output.push(node.id);
    collectAllChildren(node.children || [], output);
  }
}

function collectDescendantIds(id: number): number[] {
  const target = findTreeNodeById(departmentTree.value || [], id);
  if (!target) {
    return [];
  }
  const result: number[] = [];
  collectAllChildren(target.children || [], result);
  return result;
}

const onDepartmentSelect = (keys: (string | number)[]) => {
  if (!keys.length) {
    selectedDepartmentId.value = undefined;
    return;
  }
  selectedDepartmentId.value = Number(keys[0]);
};

const clearDepartmentFilter = () => {
  selectedDepartmentId.value = undefined;
};

const load = async () => {
  try {
    const [treeRes, userRes, roleRes] = await Promise.all([
      orgApi.departmentsTree(),
      orgApi.users(),
      orgApi.roles()
    ]);
    departmentTree.value = treeRes.data.data || [];
    users.value = userRes.data.data || [];
    roles.value = roleRes.data.data || [];

    if (selectedDepartmentId.value) {
      const exists = allDepartmentNodes.value.some((item) => item.id === selectedDepartmentId.value);
      if (!exists) {
        selectedDepartmentId.value = undefined;
      }
    }
  } catch (error) {
    notifyError(error);
  }
};

const openDepartment = (record?: any) => {
  departmentForm.id = record?.id;
  departmentForm.name = record?.name || '';
  departmentForm.parentId = record?.parentId;
  departmentForm.leaderUserId = record?.leaderUserId;
  departmentModal.value = true;
};

const openDepartmentWithParent = (parentId: number) => {
  departmentForm.id = undefined;
  departmentForm.name = '';
  departmentForm.parentId = parentId;
  departmentForm.leaderUserId = undefined;
  departmentModal.value = true;
};

const saveDepartment = async () => {
  if (!departmentForm.name.trim()) {
    return;
  }
  saving.value = true;
  try {
    const payload = {
      name: departmentForm.name,
      parentId: departmentForm.parentId,
      leaderUserId: departmentForm.leaderUserId
    };
    if (departmentForm.id) {
      await orgApi.updateDepartment(departmentForm.id, payload);
      notifySuccess('部门更新成功');
    } else {
      await orgApi.createDepartment(payload);
      notifySuccess('部门创建成功');
    }
    departmentModal.value = false;
    await load();
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const removeDepartment = async (record: any) => {
  try {
    await orgApi.deleteDepartment(record.id);
    notifySuccess('部门删除成功');
    await load();
  } catch (error) {
    notifyError(error);
  }
};

const openUser = (record?: any) => {
  userForm.id = record?.id;
  userForm.username = record?.username || '';
  userForm.fullName = record?.fullName || '';
  userForm.phone = record?.phone || '';
  userForm.employeeNo = record?.employeeNo || '';
  userForm.email = record?.email || '';
  userForm.gender = record?.gender;
  userForm.title = record?.title || '';
  userForm.hireDate = record?.hireDate || '';
  userForm.emergencyPhone = record?.emergencyPhone || '';
  userForm.departmentId = record?.departmentId || selectedDepartmentId.value || departmentOptions.value[0]?.id;
  userForm.dataScope = record?.dataScope || 'SELF';
  userForm.enabled = record?.enabled ?? true;
  userForm.roleIds = [...(record?.roleIds || [])];
  userModal.value = true;
};

const saveUser = async () => {
  if (!/^1\d{10}$/.test(userForm.phone)) {
    notifyError(new Error('手机号格式不正确'));
    return;
  }
  if (!userForm.username.trim() || !userForm.fullName.trim() || !userForm.departmentId) {
    return;
  }

  saving.value = true;
  try {
    const payload = { ...userForm };
    if (userForm.id) {
      await orgApi.updateUser(userForm.id, payload);
      notifySuccess('用户更新成功');
    } else {
      await orgApi.createUser(payload);
      notifySuccess('用户创建成功（默认密码 123456）');
    }
    userModal.value = false;
    await load();
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const toggleUserStatus = async (record: any) => {
  try {
    await orgApi.updateUserStatus(record.id, !record.enabled);
    notifySuccess(`用户已${record.enabled ? '禁用' : '启用'}`);
    await load();
  } catch (error) {
    notifyError(error);
  }
};

const resetPassword = async (record: any) => {
  try {
    await orgApi.resetUserPassword(record.id);
    notifySuccess(`${record.fullName || record.username} 密码已重置为 123456`);
  } catch (error) {
    notifyError(error);
  }
};

const removeUser = async (record: any) => {
  try {
    await orgApi.deleteUser(record.id);
    notifySuccess('用户删除成功');
    await load();
  } catch (error) {
    notifyError(error);
  }
};

onMounted(load);
</script>

<style scoped>
.org-page {
  display: grid;
  gap: 10px;
}

.org-workspace {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 10px;
  min-height: calc(100vh - 200px);
}

.dept-pane,
.user-pane {
  border: 1px solid #e8edf5;
  border-radius: 10px;
  background: #fff;
  min-width: 0;
}

.pane-toolbar {
  display: flex;
  gap: 8px;
  padding: 10px;
  border-bottom: 1px solid #edf2f8;
  align-items: center;
}

.user-toolbar {
  justify-content: space-between;
}

.user-search {
  width: 260px;
}

.dept-all {
  margin: 8px 10px 0;
  padding: 7px 10px;
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
