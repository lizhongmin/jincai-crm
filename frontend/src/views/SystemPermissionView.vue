<template>
  <div class="permission-page">
    <a-card class="section-card" :bordered="false">
      <div class="permission-toolbar">
        <a-button type="primary" @click="openPermissionForm()">
          <template #icon><plus-outlined /></template>
          新增顶级菜单
        </a-button>
        <a-button @click="expandAll">
          <template #icon><down-outlined /></template>
          全部展开
        </a-button>
        <a-button @click="collapseAll">
          <template #icon><up-outlined /></template>
          全部收起
        </a-button>
        <a-button @click="load">
          <template #icon><reload-outlined /></template>
          刷新
        </a-button>
        <div class="toolbar-spacer" />
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索名称或编码"
          allow-clear
          style="width: 260px"
          @search="onSearch"
          @change="onSearchChange"
        />
      </div>

      <a-table
        class="permission-table"
        :columns="columns"
        :data-source="flattenedTreeData"
        :loading="loading"
        :pagination="false"
        :scroll="{ y: 'calc(100vh - 240px)' }"
        size="middle"
        row-key="id"
        :row-class-name="getRowClassName"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'name'">
            <span class="permission-name-cell" :style="{ paddingLeft: `${(record.level || 0) * 24}px` }">
              <span
                class="expand-icon"
                :class="{ 'has-children': record.children && record.children.length > 0, 'is-expanded': isExpanded(record.id) }"
                @click="toggleExpand(record)"
              >
                <right-outlined />
              </span>
              <span class="name-text">{{ record.name }}</span>
            </span>
          </template>
          <template v-else-if="column.dataIndex === 'type'">
            <a-tag v-if="record.type === 'MENU'" color="blue">菜单</a-tag>
            <a-tag v-else color="green">权限点</a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'menuPath'">
            <span v-if="record.type === 'MENU'">{{ record.menuPath || '-' }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'actions'">
            <a-space size="small">
              <a-button type="link" size="small" @click="openPermissionForm(record)">编辑</a-button>
              <a-button v-if="record.type === 'MENU'" type="link" size="small" @click="openPermissionForm(null, record)">新增子节点</a-button>
              <a-popconfirm
                title="确认删除该权限？"
                @confirm="deletePermission(record)"
              >
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-drawer
      v-model:open="permissionModal"
      :title="permissionForm.id ? '编辑权限' : '新增权限'"
      placement="right"
      :width="520"
    >
      <template #extra>
        <a-space>
          <a-button @click="permissionModal = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="savePermission">保存</a-button>
        </a-space>
      </template>

      <a-form layout="vertical">
        <a-form-item label="类型" required>
          <a-radio-group v-model:value="permissionForm.type">
            <a-radio value="MENU">菜单</a-radio>
            <a-radio value="BUTTON">权限点</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="名称" required>
          <a-input v-model:value="permissionForm.name" placeholder="请输入名称" />
        </a-form-item>

        <a-form-item label="编码" required>
          <a-input v-model:value="permissionForm.code" placeholder="如：MENU_ORG / BTN_ORG_CREATE" />
        </a-form-item>

        <a-form-item v-if="permissionForm.type === 'MENU'" label="路由路径">
          <a-input v-model:value="permissionForm.menuPath" placeholder="如：/system/org" />
        </a-form-item>

        <a-form-item label="上级节点">
          <a-tree-select
            v-model:value="permissionForm.parentId"
            :tree-data="parentTreeData"
            placeholder="请选择上级节点"
            :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
            :field-names="{ children: 'children', label: 'name', value: 'id' }"
            tree-default-expand-all
            allow-clear
            :disabled="permissionForm.type === 'MENU' && !permissionForm.id"
          />
        </a-form-item>
      </a-form>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { DownOutlined, PlusOutlined, ReloadOutlined, RightOutlined, UpOutlined } from '@ant-design/icons-vue';
import { orgApi } from '../api/crm';
import { notifyError, notifySuccess } from '../utils/notify';

interface PermissionNode {
  id: number;
  code: string;
  name: string;
  type: string;
  menuPath?: string;
  parentId?: number;
  children?: PermissionNode[];
  level?: number;
}

const loading = ref(false);
const saving = ref(false);
const permissionModal = ref(false);
const expandedRowKeys = ref<number[]>([]);
const searchKeyword = ref('');

const permissions = ref<PermissionNode[]>([]);
const permissionForm = reactive({
  id: undefined as number | undefined,
  code: '',
  name: '',
  type: 'MENU',
  menuPath: '',
  parentId: undefined as number | undefined
});

const columns = [
  { title: '名称', dataIndex: 'name', width: 250 },
  { title: '编码', dataIndex: 'code', width: 200 },
  { title: '类型', dataIndex: 'type', width: 100 },
  { title: '路由路径', dataIndex: 'menuPath', width: 150 },
  { title: '操作', dataIndex: 'actions', width: 200 }
];

const normalizedKeyword = computed(() => searchKeyword.value.trim().toLowerCase());

const collectExpandableKeys = (nodes: PermissionNode[]): number[] => {
  const keys: number[] = [];

  const traverse = (items: PermissionNode[]) => {
    items.forEach(node => {
      if (node.children && node.children.length > 0) {
        keys.push(node.id);
        traverse(node.children);
      }
    });
  };

  traverse(nodes);
  return keys;
};

// Check if a row is expanded
const isExpanded = (key: number) => {
  return expandedRowKeys.value.includes(key);
};

// Toggle expand for a row
const toggleExpand = (record: PermissionNode) => {
  // Check if node has children
  if (!record.children || record.children.length === 0) {
    return;
  }

  const key = record.id;
  if (isExpanded(key)) {
    // Collapse - remove key and all children
    const keysToRemove = new Set<number>();
    const collectKeys = (node: PermissionNode) => {
      keysToRemove.add(node.id);
      if (node.children) {
        node.children.forEach(collectKeys);
      }
    };
    collectKeys(record);
    expandedRowKeys.value = expandedRowKeys.value.filter(k => !keysToRemove.has(k));
  } else {
    // Expand - add key
    expandedRowKeys.value = [...expandedRowKeys.value, key];
  }
};

const expandAll = () => {
  expandedRowKeys.value = collectExpandableKeys(permissions.value);
};

const collapseAll = () => {
  expandedRowKeys.value = [];
};

const onSearch = () => {
  if (normalizedKeyword.value) {
    expandAll();
  }
};

const onSearchChange = () => {
  if (normalizedKeyword.value) {
    expandAll();
  }
};

// Get row class name based on node level
const getRowClassName = (record: PermissionNode) => {
  return `permission-row-level-${record.level || 0}`;
};

// Flatten tree data for table display based on expanded state
const filteredPermissions = computed(() => {
  if (!normalizedKeyword.value) {
    return permissions.value;
  }

  const filterTree = (nodes: PermissionNode[]): PermissionNode[] => {
    return nodes.reduce<PermissionNode[]>((result, node) => {
      const children = node.children ? filterTree(node.children) : [];
      const matched = [node.name, node.code]
        .filter(Boolean)
        .some(value => value.toLowerCase().includes(normalizedKeyword.value));

      if (matched || children.length > 0) {
        result.push({
          ...node,
          children
        });
      }

      return result;
    }, []);
  };

  return filterTree(permissions.value);
});

// Flatten tree data for table display based on expanded state
const flattenedTreeData = computed(() => {
  const result: PermissionNode[] = [];

  // Recursive function to flatten tree structure
  const flattenTree = (nodes: PermissionNode[], level = 0) => {
    if (!nodes || nodes.length === 0) return;

    nodes.forEach(node => {
      // Add current node with level information
      result.push({ ...node, level });

      // If node has children and is expanded, traverse children
      if (node.children && node.children.length > 0 && isExpanded(node.id)) {
        flattenTree(node.children, level + 1);
      }
    });
  };

  // Start flattening from root nodes
  flattenTree(filteredPermissions.value);

  return result;
});

// Parent tree data for select dropdown (only menus)
const parentTreeData = computed(() => {
  const result: PermissionNode[] = [];

  const traverse = (nodes: PermissionNode[]): PermissionNode[] => {
    return nodes.map(node => {
      if (node.type === 'MENU') {
        return {
          ...node,
          children: node.children ? traverse(node.children.filter(child => child.type === 'MENU')) : []
        };
      }
      return node;
    }).filter(node => node.type === 'MENU');
  };

  const resultData = traverse(permissions.value);
  console.log('parentTreeData computed:', resultData);
  return resultData;
});

const load = async () => {
  console.log('Loading permission tree data...');
  loading.value = true;
  try {
    const { data } = await orgApi.permissionsTreeView();
    console.log('Permission tree data loaded:', data.data);
    permissions.value = data.data || [];
    // Auto-expand all top-level menus
    expandedRowKeys.value = (permissions.value || [])
      .filter(node => node.parentId == null)
      .map(node => node.id);
  } catch (error) {
    console.error('Load permission tree error:', error);
    notifyError(error);
  } finally {
    loading.value = false;
  }
};

const openPermissionForm = (record?: PermissionNode | null, parent?: PermissionNode | null) => {
  if (record) {
    // Edit existing permission
    permissionForm.id = record.id;
    permissionForm.code = record.code;
    permissionForm.name = record.name;
    permissionForm.type = record.type;
    permissionForm.menuPath = record.menuPath || '';
    permissionForm.parentId = record.parentId;
  } else if (parent) {
    // Add child to parent
    permissionForm.id = undefined;
    permissionForm.code = '';
    permissionForm.name = '';
    permissionForm.type = 'BUTTON';
    permissionForm.menuPath = '';
    permissionForm.parentId = parent.id;
  } else {
    // Add new top-level menu
    permissionForm.id = undefined;
    permissionForm.code = '';
    permissionForm.name = '';
    permissionForm.type = 'MENU';
    permissionForm.menuPath = '';
    permissionForm.parentId = undefined;
  }
  permissionModal.value = true;
};

const savePermission = async () => {
  if (!permissionForm.name.trim() || !permissionForm.code.trim()) {
    return;
  }

  saving.value = true;
  try {
    const payload = {
      code: permissionForm.code,
      name: permissionForm.name,
      type: permissionForm.type,
      menuPath: permissionForm.type === 'MENU' ? permissionForm.menuPath : undefined,
      parentId: permissionForm.parentId
    };

    if (permissionForm.id) {
      await orgApi.updatePermission(permissionForm.id, payload);
      notifySuccess('权限更新成功');
    } else {
      await orgApi.createPermission(payload);
      notifySuccess('权限创建成功');
    }

    permissionModal.value = false;
    await load();
  } catch (error) {
    notifyError(error);
  } finally {
    saving.value = false;
  }
};

const deletePermission = async (record: PermissionNode) => {
  try {
    await orgApi.deletePermission(record.id);
    notifySuccess('权限删除成功');
    await load();
  } catch (error) {
    notifyError(error);
  }
};

onMounted(load);
</script>

<style scoped>
.permission-page {
  display: grid;
  gap: 10px;
}

.permission-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.toolbar-spacer {
  flex: 1 1 auto;
}

.permission-table :deep(.ant-table-thead > tr > th) {
  background: #f6f9fc;
}

.permission-table :deep(.ant-table-tbody > tr:hover) {
  background-color: #f8fbff;
}

.permission-table :deep(.ant-table-tbody > tr.ant-table-row-selected) {
  background-color: #e6f4ff;
}

.permission-name-cell {
  display: flex;
  align-items: center;
  height: 100%;
}

.expand-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 20px;
  width: 20px;
  height: 20px;
  margin-right: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #bfbfbf;
  border-radius: 4px;
}

.expand-icon.has-children {
  color: #8c8c8c;
}

.expand-icon.has-children:hover {
  color: #1677ff;
  background: #e6f4ff;
}

.expand-icon.is-expanded {
  transform: rotate(90deg);
}

/* 没有子节点时隐藏箭头图标，但保留占位 */
.expand-icon:not(.has-children) {
  visibility: hidden;
}

.indent {
  display: none;
}

.name-text {
  margin-left: 4px;
  font-weight: 500;
}

.permission-type-tag {
  margin-right: 4px;
}

.action-button {
  padding: 2px 8px;
}

/* 区分不同层级行的背景色，增强层级感 */
.permission-table :deep(.permission-row-level-0) {
  background-color: #ffffff;
  font-weight: 500;
}

.permission-table :deep(.permission-row-level-1) {
  background-color: #fafcff;
}

.permission-table :deep(.permission-row-level-2) {
  background-color: #f5f7fa;
}

.permission-table :deep(.permission-row-level-3) {
  background-color: #f0f2f5;
}


</style>
