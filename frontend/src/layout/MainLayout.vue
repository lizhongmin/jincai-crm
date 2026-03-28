<template>
  <a-layout class="pro-layout">
    <a-layout-sider
      v-model:collapsed="collapsed"
      collapsible
      width="232"
      class="pro-sider"
      :trigger="null"
    >
      <div class="brand">
        <div class="logo-dot"></div>
        <div v-if="!collapsed" class="brand-text">
          <strong>袋鼠旅客通</strong>
          <span>旅业智能管家</span>
        </div>
      </div>

      <a-menu
        theme="dark"
        mode="inline"
        :selected-keys="[activeMenuKey]"
        :open-keys="openMenuKeys"
        @click="onMenuClick"
      >
        <template v-for="item in menuItems" :key="item.key">
          <a-sub-menu v-if="item.children?.length" :key="item.key">
            <template #icon>
              <component :is="item.icon" />
            </template>
            <template #title>{{ item.title }}</template>
            <a-menu-item v-for="child in item.children" :key="child.path">
              <span>{{ child.title }}</span>
            </a-menu-item>
          </a-sub-menu>
          <a-menu-item v-else :key="item.path">
            <component :is="item.icon" />
            <span>{{ item.title }}</span>
          </a-menu-item>
        </template>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <a-layout-header class="pro-header">
        <div class="left-head">
          <a-button type="text" @click="collapsed = !collapsed">
            <menu-unfold-outlined v-if="collapsed" />
            <menu-fold-outlined v-else />
          </a-button>
          <h2 class="head-title">{{ pageTitle }}</h2>
        </div>

        <div class="right-head">
          <a-badge :count="unreadCount" size="small">
            <a-button shape="circle" @click="openNotifications">
              <template #icon><bell-outlined /></template>
            </a-button>
          </a-badge>
          <a-dropdown>
            <a class="user-link" @click.prevent>
              <a-avatar size="small" style="background: #1677ff">{{ userInitial }}</a-avatar>
              <span>{{ auth.profile?.username || '未登录' }}</span>
            </a>
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile" @click="router.push('/profile')">个人中心</a-menu-item>
                <a-menu-item key="logout" @click="logout">退出登录</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <a-layout-content class="pro-content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>

  <a-drawer v-model:open="notificationOpen" title="系统通知" placement="right" width="420">
    <a-list :data-source="notifications" :locale="{ emptyText: '暂无通知' }">
      <template #renderItem="{ item }">
        <a-list-item>
          <a-list-item-meta :title="item.content" :description="item.createdAt || '-'" />
          <template #actions>
            <a-button v-if="!item.readFlag" type="link" @click="markRead(item.id)">标记已读</a-button>
            <a-tag v-else color="green">已读</a-tag>
          </template>
        </a-list-item>
      </template>
    </a-list>
  </a-drawer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  BellOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  PieChartOutlined,
  UserOutlined,
  ShopOutlined,
  SnippetsOutlined,
  PayCircleOutlined,
  BarChartOutlined,
  ApartmentOutlined,
  SettingOutlined
} from '@ant-design/icons-vue';
import { useAuthStore } from '../stores/auth';
import { authApi, notificationApi, permissionApi } from '../api/crm';
import { notifyError } from '../utils/notify';

/**
 * 菜单节点类型定义
 * 用于构建左侧导航菜单的树形结构
 */
type MenuNode = {
  key: string;              // 菜单项唯一标识
  title: string;            // 菜单项显示名称
  path?: string;            // 路由路径
  permissionPath?: string;  // 权限检查路径（可与path不同）
  icon?: any;               // 菜单图标组件
  children?: MenuNode[];    // 子菜单项
};

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const collapsed = ref(false);
const notificationOpen = ref(false);
const notifications = ref<any[]>([]);

/**
 * 应用菜单配置
 * 定义了整个应用的导航菜单结构和权限控制
 * 注意：系统管理模块的子菜单项通过 permissionPath 控制权限访问
 */
const allMenuItems: MenuNode[] = [
  { key: '/dashboard', path: '/dashboard', title: '经营看板', icon: PieChartOutlined },
  { key: '/customers', path: '/customers', title: '客户管理', icon: UserOutlined },
  { key: '/products', path: '/products', title: '产品团期', icon: ShopOutlined },
  { key: '/orders', path: '/orders', title: '订单管理', icon: SnippetsOutlined },
  { key: '/finance', path: '/finance', title: '收付审核', icon: PayCircleOutlined },
  { key: '/reports', path: '/reports', title: 'BI 报表', icon: BarChartOutlined },
  { key: '/workflow', path: '/workflow', title: '流程模板', icon: ApartmentOutlined },
  {
    key: 'system',
    title: '系统管理',
    icon: SettingOutlined,
    children: [
      { key: '/system/org', path: '/system/org', title: '组织架构', permissionPath: '/system/org' },
      { key: '/system/role', path: '/system/role', title: '角色权限', permissionPath: '/system/role' },
      { key: '/system/permission', path: '/system/permission', title: '菜单权限', permissionPath: '/system/permission' },
      { key: '/system/security', path: '/system/security', title: '登录安全', permissionPath: '/system/security' },
      { key: '/system/audit', path: '/system/audit', title: '审计日志', permissionPath: '/system/audit' }
    ]
  },
];

/**
 * 检查菜单项权限
 * 根据用户权限数据判断是否显示某个菜单项
 * @param item 菜单项
 * @returns boolean 是否有权限访问该菜单项
 */
const hasPermission = (item: MenuNode) => {
  // 如果没有权限数据（如首次加载），默认显示所有菜单
  if (!auth.allowedMenuPaths.length) {
    return true;
  }
  // 获取权限检查路径（优先使用permissionPath，否则使用path）
  const target = String(item.permissionPath || item.path || '');
  // 如果没有指定路径，默认允许访问
  return target ? auth.allowedMenuPaths.includes(target) : true;
};

/**
 * 过滤菜单节点
 * 递归过滤菜单树，只保留用户有权限访问的菜单项
 * @param nodes 菜单节点数组
 * @returns 过滤后的菜单节点数组
 */
const filterMenuNodes = (nodes: MenuNode[]): MenuNode[] =>
  nodes
    .map((node) => {
      // 叶子节点（无子菜单）直接检查权限
      if (!node.children?.length) {
        return hasPermission(node) ? node : null;
      }
      // 父节点需要递归检查子节点权限
      const children = filterMenuNodes(node.children);
      // 如果没有子节点有权限，当前父节点也不显示
      if (!children.length) {
        return null;
      }
      // 返回包含有权限子节点的父节点
      return { ...node, children };
    })
    .filter(Boolean) as MenuNode[];

/**
 * 计算用户有权限访问的菜单项
 * 基于权限过滤后的菜单项列表
 */
const menuItems = computed(() => filterMenuNodes(allMenuItems));

/**
 * 计算所有叶子菜单路径
 * 用于在用户无权限访问当前页面时进行重定向
 */
const menuLeafPaths = computed(() => {
  const result: string[] = [];
  const walk = (nodes: MenuNode[]) => {
    for (const node of nodes) {
      if (node.children?.length) {
        // 递归处理子菜单
        walk(node.children);
      } else if (node.path) {
        // 收集叶子节点路径
        result.push(node.path);
      }
    }
  };
  walk(menuItems.value);
  return result;
});

/**
 * 计算当前激活的菜单项
 * 用于设置菜单高亮状态
 */
const activeMenuKey = computed(() => {
  // 特殊路径处理
  if (route.path === '/org') return '/system/org';
  if (route.path.startsWith('/orders/')) return '/orders';
  return route.path;
});

/**
 * 计算当前展开的菜单项
 * 用于控制侧边栏菜单的展开/收起状态
 */
const openMenuKeys = computed(() => {
  // 菜单收起时不需要展开任何项
  if (collapsed.value) {
    return [];
  }
  // 根据当前路由自动展开对应的父菜单
  if (activeMenuKey.value.startsWith('/system/')) {
    return ['system'];
  }
  if (activeMenuKey.value.startsWith('/products/')) {
    return ['products'];
  }
  return [];
});

/**
 * 计算页面标题
 * 从路由元信息中获取页面标题
 */
const pageTitle = computed(() => String(route.meta.title || '工作台'));

/**
 * 计算未读通知数量
 * 用于在通知按钮上显示未读数量徽章
 */
const unreadCount = computed(() => notifications.value.filter((n) => !n.readFlag).length);

/**
 * 计算用户头像初始字符
 * 用于在用户头像上显示用户名首字母
 */
const userInitial = computed(() => (auth.profile?.username?.[0] || 'U').toUpperCase());

/**
 * 菜单项点击处理函数
 * 负责页面导航跳转
 * @param key 菜单项key
 */
const onMenuClick = ({ key }: { key: string }) => {
  router.push(key);
};

/**
 * 加载用户资料
 * 从后端获取当前登录用户的完整资料
 */
const loadProfile = async () => {
  const { data } = await authApi.me();
  if (data.success) {
    auth.setProfile(data.data);
  }
};

/**
 * 加载用户权限数据
 * 从后端获取用户的菜单权限和按钮权限
 */
const loadPermissions = async () => {
  const { data } = await permissionApi.menus();
  const permissions = data.data || [];

  // 分离菜单路径和按钮权限
  const menuPaths = Array.from(
    new Set(
      permissions
        .map((item: any) => String(item.menuPath || '').trim())
        .filter((path: string) => path.startsWith('/'))
    )
  );

  const buttonCodes = Array.from(
    new Set(
      permissions
        .map((item: any) => String(item.code || '').trim())
        .filter((code: string) => code && code !== '')
    )
  );

  // 更新权限存储
  auth.setAllowedMenuPaths(menuPaths);
  auth.setButtonPermissions(buttonCodes);
  auth.setAllPermissions(permissions);

  // 权限检查：如果用户无权限访问当前页面，重定向到第一个可用页面
  const currentPermissionPath = String(route.meta.permissionPath || route.path);
  if (menuPaths.length > 0 && !menuPaths.includes(currentPermissionPath)) {
    await router.replace(menuLeafPaths.value[0] || '/dashboard');
  }
};

/**
 * 加载通知列表
 * 从后端获取用户的通知消息
 */
const loadNotifications = async () => {
  const { data } = await notificationApi.list();
  notifications.value = data.data || [];
};

/**
 * 打开通知抽屉
 * 显示系统通知列表
 */
const openNotifications = async () => {
  notificationOpen.value = true;
  await loadNotifications();
};

/**
 * 标记通知为已读
 * @param id 通知ID
 */
const markRead = async (id: number) => {
  await notificationApi.read(id);
  await loadNotifications();
};

/**
 * 用户登出处理
 * 清除认证信息并跳转到登录页
 */
const logout = () => {
  auth.logout();
  router.replace('/login');
};

/**
 * 组件挂载时执行初始化操作
 * 并行加载用户资料、权限数据和通知列表
 */
onMounted(async () => {
  try {
    await Promise.all([loadProfile(), loadPermissions(), loadNotifications()]);
  } catch (error) {
    notifyError(error);
  }
});
</script>

<style scoped>
.pro-layout {
  min-height: 100vh;
}

.pro-sider {
  border-right: 1px solid rgba(255, 255, 255, 0.07);
}

.brand {
  height: 66px;
  margin: 10px 12px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(73, 145, 255, 0.26), rgba(8, 23, 46, 0.66));
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
}

.logo-dot {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7ec9ff, #1677ff);
  box-shadow: 0 0 0 4px rgba(88, 169, 255, 0.16);
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-text strong {
  color: #f3f8ff;
  font-size: 14px;
}

.brand-text span {
  color: #9fb2cb;
  font-size: 11px;
}

.pro-header {
  background: rgba(255, 255, 255, 0.78);
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(12px);
  padding: 0 18px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.left-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.head-title {
  margin: 0;
  line-height: 1.2;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.right-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--ink);
}

.pro-content {
  padding: 14px;
}
</style>
