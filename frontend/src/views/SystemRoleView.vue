<template>
  <div class="role-page">
    <a-card class="section-card role-card" :bordered="false">
      <div class="role-layout">
        <aside class="role-list-pane">
          <div class="role-toolbar">
            <a-input v-model:value="roleKeyword" allow-clear placeholder="通过角色名称/编码搜索">
              <template #prefix>
                <search-outlined />
              </template>
            </a-input>
            <a-tooltip title="新增角色">
              <a-button @click="openRole()">
                <template #icon><plus-outlined /></template>
              </a-button>
            </a-tooltip>
            <a-tooltip title="刷新">
              <a-button @click="load">
                <template #icon><reload-outlined /></template>
              </a-button>
            </a-tooltip>
          </div>

          <div class="role-sub-info">共 {{ roles.length }} 个角色</div>

          <div class="role-list">
            <div
              v-for="role in filteredRoles"
              :key="role.id"
              class="role-item"
              :class="{ active: role.id === selectedRoleId }"
              @click="selectRole(role.id)"
            >
              <div class="role-main">
                <div class="role-name">{{ role.name }}</div>
                <div class="role-code">{{ role.code }}</div>
              </div>
              <a-space class="role-actions" size="small" @click.stop>
                <a-tooltip title="编辑角色">
                  <a-button type="text" size="small" :disabled="isAdminRole(role)" @click.stop="openRole(role)">
                    <template #icon><edit-outlined /></template>
                  </a-button>
                </a-tooltip>
                <a-popconfirm v-if="!isAdminRole(role)" title="确认删除该角色？" @confirm="removeRole(role)">
                  <a-button type="text" size="small" danger @click.stop>
                    <template #icon><delete-outlined /></template>
                  </a-button>
                </a-popconfirm>
              </a-space>
            </div>

            <a-empty v-if="!filteredRoles.length && !roleLoading" :image="false" description="暂无角色" />
          </div>
        </aside>

        <section class="role-detail-pane">
          <template v-if="activeRole">
            <div class="detail-head card-surface">
              <div class="detail-head-main">
                <div class="detail-title-line">
                  <h3>{{ activeRole.name }}</h3>
                  <a-tag v-if="isAdminRole(activeRole)" color="blue">系统管理员角色</a-tag>
                </div>
                <p>{{ activeRole.code }}</p>
              </div>
              <div class="detail-head-actions">
                <a-button type="link" :disabled="isAdminRole(activeRole)" @click="openRole(activeRole)">编辑角色</a-button>
                <a-button :disabled="!grantEditable || !grantChanged" @click="resetGrantSelection">撤销更改</a-button>
                <a-button type="primary" :loading="savingGrant" :disabled="!grantEditable || !grantChanged" @click="saveGrant">更新</a-button>
              </div>
            </div>

            <div class="metrics-row">
              <div class="metric-item card-surface">
                <span class="metric-label">角色成员</span>
                <strong class="metric-value">{{ memberTotal }}</strong>
              </div>
              <div class="metric-item card-surface">
                <span class="metric-label">已分配权限</span>
                <strong class="metric-value">{{ grantPermissionIds.length }}</strong>
              </div>
              <div class="metric-item card-surface">
                <span class="metric-label">覆盖模块</span>
                <strong class="metric-value">{{ activeModuleCount }}</strong>
              </div>
              <div class="metric-item card-surface" :class="{ warn: grantChanged }">
                <span class="metric-label">变更状态</span>
                <strong class="metric-value">{{ grantChanged ? '未保存' : '已同步' }}</strong>
              </div>
            </div>

            <a-tabs v-model:activeKey="detailTab" class="detail-tabs role-detail-tabs">
              <a-tab-pane key="permission" tab="权限配置">
                <div class="permission-pane">
                  <div class="permission-scroll-wrap">
                    <div class="permission-config card-surface">
                      <div class="data-scope-section">
                        <div class="section-title">数据权限</div>
                        <a-radio-group v-model:value="activeDataScope" class="data-scope-radio" :disabled="!grantEditable">
                          <a-radio value="ALL">全部数据</a-radio>
                          <a-radio value="DEPARTMENT_TREE">本部门及子部门数据</a-radio>
                          <a-radio value="DEPARTMENT">本部门数据</a-radio>
                          <a-radio value="SELF">仅本人数据</a-radio>
                        </a-radio-group>
                      </div>

                      <div class="permission-tree-section">
                        <div class="section-title">功能权限</div>
                        <permission-tree-panel v-model:checkedKeys="grantPermissionIds" :groups="permissionGroups" :disabled="!grantEditable" />
                      </div>
                    </div>
                  </div>

                </div>
              </a-tab-pane>

              <a-tab-pane key="member" tab="成员列表">
                <a-table
                  class="member-table"
                  :columns="memberColumns"
                  :data-source="activeRoleMembers"
                  row-key="id"
                  size="small"
                  :loading="memberLoading"
                  :scroll="{ x: 900 }"
                  :pagination="{
                    current: memberPage,
                    pageSize: memberPageSize,
                    total: memberTotal,
                    showSizeChanger: true,
                    pageSizeOptions: ['10', '20', '50'],
                    showTotal: (total: number) => `共 ${total} 条`
                  }"
                  @change="onMemberPageChange"
                />
              </a-tab-pane>
            </a-tabs>
          </template>

          <a-empty v-else description="请先选择左侧角色" />
        </section>
      </div>
    </a-card>

    <a-drawer v-model:open="roleModal" :title="roleForm.id ? '编辑角色' : '新增角色'" placement="right" :width="520">
      <template #extra>
        <a-space>
          <a-button @click="roleModal = false">取消</a-button>
          <a-button type="primary" :loading="savingRole" @click="saveRole">保存</a-button>
        </a-space>
      </template>

      <a-form layout="vertical">
        <a-form-item label="角色编码" required>
          <a-input v-model:value="roleForm.code" placeholder="如：SALES_MANAGER" :disabled="isAdminRole(roleForm)" />
        </a-form-item>
        <a-form-item label="角色名称" required>
          <a-input v-model:value="roleForm.name" placeholder="如：销售经理" :disabled="isAdminRole(roleForm)" />
        </a-form-item>
        <a-form-item label="数据权限范围" required>
          <a-select v-model:value="roleForm.dataScope" placeholder="请选择数据权限范围" :disabled="isAdminRole(roleForm)">
            <a-select-option value="SELF">仅本人</a-select-option>
            <a-select-option value="DEPARTMENT">本部门</a-select-option>
            <a-select-option value="DEPARTMENT_TREE">本部门及子部门</a-select-option>
            <a-select-option value="ALL">全部数据</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="描述">
          <a-input v-model:value="roleForm.description" :disabled="isAdminRole(roleForm)" />
        </a-form-item>
      </a-form>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { DeleteOutlined, EditOutlined, PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';
import { computed, onMounted, reactive, ref, watch } from 'vue';
import PermissionTreePanel from '../components/org/PermissionTreePanel.vue';
import { orgApi } from '../api/crm';
import { notifyError, notifySuccess } from '../utils/notify';

const roleKeyword = ref('');
const detailTab = ref('permission');
const roleModal = ref(false);
const savingRole = ref(false);
const savingGrant = ref(false);

const roles = ref<any[]>([]);
const roleLoading = ref(false);
const roleMembers = ref<any[]>([]);
const memberTotal = ref(0);
const memberPage = ref(1);
const memberPageSize = ref(10);
const memberLoading = ref(false);
const permissionGroups = ref<any[]>([]);
const selectedRoleId = ref<string>();
const grantPermissionIds = ref<string[]>([]);
const grantOriginalPermissionIds = ref<string[]>([]);
const activeDataScope = ref('SELF');

const roleForm = reactive({ id: undefined as string | undefined, code: '', name: '', dataScope: 'SELF', description: '' });

const dataScopeLabels: Record<string, string> = {
  SELF: '仅本人',
  DEPARTMENT: '本部门',
  DEPARTMENT_TREE: '本部门数据',
  ALL: '全部数据'
};

const memberColumns = [
  { title: '账号', dataIndex: 'username', width: 140 },
  { title: '姓名', dataIndex: 'fullName', width: 130 },
  { title: '手机号', dataIndex: 'phone', width: 150 },
  { title: '部门', dataIndex: 'departmentName', width: 180, customRender: ({ text }: any) => text || '-' },
  {
    title: '数据范围',
    dataIndex: 'dataScope',
    width: 160,
    customRender: ({ text }: any) => dataScopeLabels[String(text || '')] || text || '-'
  },
  { title: '状态', dataIndex: 'enabled', width: 100, customRender: ({ text }: any) => (text ? '启用' : '禁用') }
];

const activeRole = computed(() => roles.value.find((item) => item.id === selectedRoleId.value));
const filteredRoles = computed(() => {
  const kw = roleKeyword.value.trim().toLowerCase();
  if (!kw) return roles.value;
  return roles.value.filter(
    (r) => r.name?.toLowerCase().includes(kw) || r.code?.toLowerCase().includes(kw)
  );
});
const activeRoleMembers = computed(() => roleMembers.value);

const activeModuleCount = computed(() => {
  const granted = new Set(grantPermissionIds.value || []);
  return (permissionGroups.value || []).filter((group: any) =>
    (group.subMenus || []).some((sub: any) =>
      (sub.menuPermission && granted.has(sub.menuPermission.id)) ||
      (sub.actions || []).some((a: any) => granted.has(a.id))
    ) || (group.menuPermission && granted.has(group.menuPermission.id))
  ).length;
});

const isAdminRole = (role?: any) => String(role?.code || '').toUpperCase() === 'ADMIN';
const grantEditable = computed(() => !!activeRole.value && !isAdminRole(activeRole.value));

const grantChanged = computed(() => {
  if (!grantEditable.value) {
    return false;
  }
  if (activeRole.value && activeDataScope.value !== activeRole.value.dataScope) {
    return true;
  }
  const current = Array.from(new Set(grantPermissionIds.value || [])).sort();
  const original = Array.from(new Set(grantOriginalPermissionIds.value || [])).sort();
  if (current.length !== original.length) {
    return true;
  }
  return current.some((id, index) => id !== original[index]);
});

const selectRole = (roleId: string) => {
  selectedRoleId.value = roleId;
};

const loadRoles = async () => {
  roleLoading.value = true;
  try {
    const { data } = await orgApi.roles();
    roles.value = data.data || [];

    if (selectedRoleId.value && !roles.value.some((item) => item.id === selectedRoleId.value)) {
      selectedRoleId.value = roles.value[0]?.id;
    }
    if (!selectedRoleId.value && roles.value.length) {
      selectedRoleId.value = roles.value[0].id;
    }
  } catch (error) {
    notifyError(error);
  } finally {
    roleLoading.value = false;
  }
};

const loadRoleMembers = async (roleId?: string) => {
  if (!roleId) {
    roleMembers.value = [];
    memberTotal.value = 0;
    return;
  }
  memberLoading.value = true;
  try {
    const { data } = await orgApi.usersPage({
      page: memberPage.value,
      size: memberPageSize.value,
      roleId
    });
    roleMembers.value = data.data?.items || [];
    memberTotal.value = Number(data.data?.total || 0);
  } catch (error) {
    notifyError(error);
  } finally {
    memberLoading.value = false;
  }
};

const loadGrantPermissions = async (roleId?: string) => {
  if (!roleId) {
    grantPermissionIds.value = [];
    grantOriginalPermissionIds.value = [];
    return;
  }
  try {
    const { data } = await orgApi.rolePermissions(roleId);
    grantPermissionIds.value = data.data || [];
    grantOriginalPermissionIds.value = [...grantPermissionIds.value];

    const role = roles.value.find(r => r.id === roleId);
    if (role) {
      activeDataScope.value = role.dataScope || 'SELF';
    }
  } catch (error) {
    notifyError(error);
  }
};

watch(selectedRoleId, (roleId) => {
  void loadGrantPermissions(roleId);
  memberPage.value = 1;
  void loadRoleMembers(roleId);
});

// roleKeyword 在前端过滤，无需 watch 触发请求

const loadPermissionTree = async () => {
  const { data } = await orgApi.permissionsTree();
  permissionGroups.value = data.data || [];
};


const onMemberPageChange = (pagination: { current?: number; pageSize?: number }) => {
  memberPage.value = pagination.current || 1;
  memberPageSize.value = pagination.pageSize || 10;
  void loadRoleMembers(selectedRoleId.value);
};

const load = async () => {
  try {
    await Promise.all([
      loadRoles(),
      loadPermissionTree()
    ]);
    await loadRoleMembers(selectedRoleId.value);
  } catch (error) {
    notifyError(error);
  }
};

const openRole = (record?: any) => {
  roleForm.id = record?.id;
  roleForm.code = record?.code || '';
  roleForm.name = record?.name || '';
  roleForm.dataScope = record?.dataScope || 'SELF';
  roleForm.description = record?.description || '';
  roleModal.value = true;
};

const saveRole = async () => {
  if (!roleForm.code.trim() || !roleForm.name.trim() || !roleForm.dataScope) {
    return;
  }
  savingRole.value = true;
  try {
    const payload = {
      code: roleForm.code,
      name: roleForm.name,
      dataScope: roleForm.dataScope,
      description: roleForm.description
    };

    if (roleForm.id) {
      await orgApi.updateRole(roleForm.id, payload);
      notifySuccess('角色更新成功');
      selectedRoleId.value = roleForm.id;
    } else {
      const { data } = await orgApi.createRole(payload);
      notifySuccess('角色创建成功');
      selectedRoleId.value = data.data?.id;
    }

    roleModal.value = false;
    await loadRoles();
  } catch (error) {
    notifyError(error);
  } finally {
    savingRole.value = false;
  }
};

const removeRole = async (record: any) => {
  try {
    await orgApi.deleteRole(record.id);
    notifySuccess('角色删除成功');
    if (selectedRoleId.value === record.id) {
      selectedRoleId.value = undefined;
    }
    await load();
  } catch (error) {
    notifyError(error);
  }
};

const resetGrantSelection = () => {
  grantPermissionIds.value = [...grantOriginalPermissionIds.value];
  if (activeRole.value) {
    activeDataScope.value = activeRole.value.dataScope || 'SELF';
  }
};

const saveGrant = async () => {
  if (!selectedRoleId.value || !activeRole.value || !grantEditable.value) {
    return;
  }
  savingGrant.value = true;
  try {
    // If dataScope changed, update role first
    if (activeDataScope.value !== activeRole.value.dataScope) {
       await orgApi.updateRole(selectedRoleId.value, {
         code: activeRole.value.code,
         name: activeRole.value.name,
         description: activeRole.value.description,
         dataScope: activeDataScope.value
       });
       // update local role data
       activeRole.value.dataScope = activeDataScope.value;
    }

    await orgApi.grantRole(selectedRoleId.value, grantPermissionIds.value);
    notifySuccess('角色权限更新成功');
    grantOriginalPermissionIds.value = [...grantPermissionIds.value];
  } catch (error) {
    notifyError(error);
  } finally {
    savingGrant.value = false;
  }
};

onMounted(load);
</script>

<style scoped>
.role-page {
  display: grid;
  gap: 10px;
}

.role-card {
  border-radius: 10px;
  overflow: hidden;
}

.role-card :deep(.ant-card-body) {
  padding: 0;
  height: 100%;
}

.role-layout {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  height: calc(100vh - 120px);
  background: #f6f9fc;
}

.role-list-pane {
  border-right: 1px solid #e8edf5;
  background: #fbfcff;
  min-width: 0;
  display: grid;
  grid-template-rows: auto auto minmax(0, 1fr);
}

.role-toolbar {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-bottom: 1px solid #edf2f8;
}

.role-toolbar :deep(.ant-input-affix-wrapper) {
  border-radius: 8px;
}

.role-sub-info {
  padding: 8px 12px;
  color: #6a768c;
  font-size: 12px;
  border-bottom: 1px solid #edf2f8;
}

.role-list {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 0;
  overflow-y: auto;
}

.role-item {
  border: 1px solid #edf2f8;
  border-radius: 10px;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  cursor: pointer;
  background: #fff;
  transition: all 0.2s ease;
}

.role-item:hover {
  border-color: #d6e4ff;
  background: #f6f9ff;
}

.role-item.active {
  border-color: #91caff;
  background: #eaf4ff;
  box-shadow: inset 0 0 0 1px rgba(22, 119, 255, 0.08);
}

.role-main {
  min-width: 0;
  flex: 1;
}

.role-name {
  font-weight: 600;
  color: #243042;
  line-height: 1.4;
}

.role-code {
  margin-top: 4px;
  font-size: 12px;
  color: #7a879b;
  line-height: 1.4;
}

.role-actions {
  opacity: 0;
  flex-shrink: 0;
  transition: opacity 0.2s ease;
}

.role-item:hover .role-actions,
.role-item.active .role-actions {
  opacity: 1;
}

.role-detail-pane {
  padding: 12px;
  display: grid;
  grid-template-rows: auto auto minmax(0, 1fr);
  gap: 10px;
  min-width: 0;
  min-height: 0;
}

.detail-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.detail-head-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.detail-title-line {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-head h3 {
  margin: 0;
  color: #243042;
  font-size: 20px;
}

.detail-head p {
  margin: 2px 0 0;
  color: #77839a;
  font-size: 12px;
}

.metrics-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.metric-item {
  border: 1px solid #e8edf5;
  border-radius: 8px;
  background: #fafcff;
  padding: 8px 10px;
  display: grid;
  gap: 2px;
}

.metric-item.warn {
  border-color: #ffd8a8;
  background: #fff8ed;
}

.metric-label {
  color: #6d7890;
  font-size: 12px;
}

.metric-value {
  color: #243042;
  font-size: 18px;
  line-height: 1.2;
}

.detail-tabs {
  min-height: 0;
}

.detail-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 8px;
}

.detail-tabs :deep(.ant-tabs-content-holder),
.detail-tabs :deep(.ant-tabs-content),
.detail-tabs :deep(.ant-tabs-tabpane) {
  height: 100%;
}

.permission-pane {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.permission-scroll-wrap {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-bottom: 12px;
}

.permission-config {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.data-scope-section,
.permission-tree-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-title {
  font-weight: 600;
  color: #243042;
  font-size: 15px;
  position: relative;
  padding-left: 10px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 14px;
  background: #1677ff;
  border-radius: 2px;
}

.card-surface {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  border: 1px solid #edf2f8;
}

.member-table :deep(.ant-table-thead > tr > th) {
  background: #f6f9fc;
}

@media (max-width: 1280px) {
  .metrics-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .role-layout {
    grid-template-columns: 1fr;
    min-height: auto;
    height: auto;
  }

  .role-list-pane {
    border-right: 0;
    border-bottom: 1px solid #e8edf5;
  }

  .role-list {
    max-height: 300px;
  }

  .detail-head {
    flex-direction: column;
    align-items: stretch;
  }

  .detail-head-actions {
    justify-content: flex-end;
    flex-wrap: wrap;
  }
}
</style>
