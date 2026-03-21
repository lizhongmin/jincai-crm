<template>
  <div class="org-page">
    <a-card class="section-card" :bordered="false">
      <template #title>组织与权限</template>
      <a-tabs v-model:activeKey="tab">
        <a-tab-pane key="department" tab="部门树">
          <div class="toolbar-row">
            <a-button type="primary" @click="openDepartment()">新增部门</a-button>
          </div>
          <department-tree-table
            style="margin-top: 12px"
            :items="departmentTree"
            @edit="openDepartment"
            @remove="removeDepartment"
          />
        </a-tab-pane>

        <a-tab-pane key="user" tab="用户">
          <div class="toolbar-row">
            <a-button type="primary" @click="openUser()">新增用户</a-button>
          </div>
          <user-list-table
            style="margin-top: 12px"
            :items="users"
            @edit="openUser"
            @toggle-status="toggleUserStatus"
            @reset-password="resetPassword"
            @remove="removeUser"
          />
        </a-tab-pane>

        <a-tab-pane key="role" tab="角色权限">
          <div class="toolbar-row">
            <a-button type="primary" @click="openRole()">新增角色</a-button>
          </div>
          <a-table :columns="roleCols" :data-source="roles" row-key="id">
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'actions'">
                <a-space>
                  <a-button type="link" :disabled="isAdminRole(record)" @click="openRole(record)">编辑</a-button>
                  <a-button type="link" :disabled="isAdminRole(record)" @click="openGrant(record)">配置权限</a-button>
                  <a-popconfirm v-if="!isAdminRole(record)" title="确认删除该角色？" @confirm="removeRole(record)">
                    <a-button type="link" danger>删除</a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
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

    <a-drawer
      v-model:open="grantModal"
      :title="`角色权限配置 - ${grantingRole?.name || ''}`"
      placement="right"
      :width="860"
    >
      <template #extra>
        <a-space>
          <a-button @click="grantModal = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="saveGrant">保存</a-button>
        </a-space>
      </template>
      <permission-tree-panel v-model:checkedKeys="grantPermissionIds" :groups="permissionGroups" />
    </a-drawer>

    <a-drawer v-model:open="roleModal" :title="roleForm.id ? '编辑角色' : '新增角色'" placement="right" :width="520">
      <template #extra>
        <a-space>
          <a-button @click="roleModal = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="saveRole">保存</a-button>
        </a-space>
      </template>
      <a-form layout="vertical">
        <a-form-item label="角色编码" required>
          <a-input v-model:value="roleForm.code" placeholder="如：SALES_MANAGER" :disabled="isAdminRole(roleForm)" />
        </a-form-item>
        <a-form-item label="角色名称" required>
          <a-input v-model:value="roleForm.name" placeholder="如：销售经理" :disabled="isAdminRole(roleForm)" />
        </a-form-item>
        <a-form-item label="描述">
          <a-input v-model:value="roleForm.description" :disabled="isAdminRole(roleForm)" />
        </a-form-item>
      </a-form>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import DepartmentTreeTable from '../components/org/DepartmentTreeTable.vue';
import PermissionTreePanel from '../components/org/PermissionTreePanel.vue';
import UserListTable from '../components/org/UserListTable.vue';
import { orgApi } from '../api/crm';
import { notifyError, notifySuccess } from '../utils/notify';

const tab = ref('department');
const saving = ref(false);
const departmentModal = ref(false);
const userModal = ref(false);
const grantModal = ref(false);
const roleModal = ref(false);

const departmentTree = ref<any[]>([]);
const departments = ref<any[]>([]);
const users = ref<any[]>([]);
const roles = ref<any[]>([]);
const permissionGroups = ref<any[]>([]);
const grantingRole = ref<any>(null);
const grantPermissionIds = ref<number[]>([]);

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
const roleForm = reactive({ id: undefined as number | undefined, code: '', name: '', description: '' });

const roleCols = [
  { title: '编码', dataIndex: 'code', width: 180 },
  { title: '角色名称', dataIndex: 'name', width: 160 },
  { title: '描述', dataIndex: 'description' },
  { title: '操作', dataIndex: 'actions', width: 240 }
];

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

const parentDepartmentOptions = computed(() => {
  if (!departmentForm.id) {
    return allDepartmentNodes.value;
  }
  const blocked = new Set<number>([departmentForm.id, ...collectDescendantIds(departmentForm.id)]);
  return allDepartmentNodes.value.filter((item) => !blocked.has(item.id));
});

const isAdminRole = (role?: any) => String(role?.code || '').toUpperCase() === 'ADMIN';

const collectDescendantIds = (id: number) => {
  const result: number[] = [];
  const walk = (nodes: any[]) => {
    for (const node of nodes || []) {
      if (node.id === id) {
        collectAllChildren(node.children || [], result);
        return true;
      }
      if (walk(node.children || [])) {
        return true;
      }
    }
    return false;
  };
  walk(departmentTree.value || []);
  return result;
};

const collectAllChildren = (nodes: any[], output: number[]) => {
  for (const node of nodes || []) {
    output.push(node.id);
    collectAllChildren(node.children || [], output);
  }
};

const load = async () => {
  try {
    const [treeRes, deptRes, userRes, roleRes, permissionTreeRes] = await Promise.all([
      orgApi.departmentsTree(),
      orgApi.departments(),
      orgApi.users(),
      orgApi.roles(),
      orgApi.permissionsTree()
    ]);
    departmentTree.value = treeRes.data.data || [];
    departments.value = deptRes.data.data || [];
    users.value = userRes.data.data || [];
    roles.value = roleRes.data.data || [];
    permissionGroups.value = permissionTreeRes.data.data || [];
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

const saveDepartment = async () => {
  if (!departmentForm.name.trim()) return;
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
  userForm.departmentId = record?.departmentId || departmentOptions.value[0]?.id;
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
  if (!userForm.username.trim() || !userForm.fullName.trim() || !userForm.departmentId) return;
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

const openRole = (record?: any) => {
  roleForm.id = record?.id;
  roleForm.code = record?.code || '';
  roleForm.name = record?.name || '';
  roleForm.description = record?.description || '';
  roleModal.value = true;
};

const saveRole = async () => {
  if (!roleForm.code.trim() || !roleForm.name.trim()) return;
  saving.value = true;
  try {
    const payload = {
      code: roleForm.code,
      name: roleForm.name,
      description: roleForm.description
    };
    if (roleForm.id) {
      await orgApi.updateRole(roleForm.id, payload);
      notifySuccess('角色更新成功');
    } else {
      await orgApi.createRole(payload);
      notifySuccess('角色创建成功');
    }
    roleModal.value = false;
    await load();
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const removeRole = async (record: any) => {
  try {
    await orgApi.deleteRole(record.id);
    notifySuccess('角色删除成功');
    await load();
  } catch (error) {
    notifyError(error);
  }
};

const openGrant = async (role: any) => {
  grantingRole.value = role;
  saving.value = true;
  try {
    const { data } = await orgApi.rolePermissions(role.id);
    grantPermissionIds.value = data.data || [];
    grantModal.value = true;
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const saveGrant = async () => {
  if (!grantingRole.value) return;
  saving.value = true;
  try {
    await orgApi.grantRole(grantingRole.value.id, grantPermissionIds.value);
    notifySuccess('角色权限更新成功');
    grantModal.value = false;
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

onMounted(load);
</script>

<style scoped>
.org-page {
  display: grid;
  gap: 16px;
}
</style>
