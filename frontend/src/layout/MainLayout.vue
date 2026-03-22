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
          <strong>JINCAI CRM</strong>
          <span>Travel Agency Console</span>
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

type MenuNode = {
  key: string;
  title: string;
  path?: string;
  permissionPath?: string;
  icon?: any;
  children?: MenuNode[];
};

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const collapsed = ref(false);
const notificationOpen = ref(false);
const notifications = ref<any[]>([]);

const allMenuItems: MenuNode[] = [
  { key: '/dashboard', path: '/dashboard', title: '经营看板', icon: PieChartOutlined },
  {
    key: 'system',
    title: '系统管理',
    icon: SettingOutlined,
    children: [
      { key: '/system/org', path: '/system/org', title: '组织架构', permissionPath: '/org' },
      { key: '/system/role', path: '/system/role', title: '角色权限', permissionPath: '/org' },
      { key: '/system/security', path: '/system/security', title: '登录安全', permissionPath: '/security' }
    ]
  },
  { key: '/customers', path: '/customers', title: '客户管理', icon: UserOutlined },
  { key: '/products', path: '/products', title: '产品团期', icon: ShopOutlined },
  { key: '/workflow', path: '/workflow', title: '流程模板', icon: ApartmentOutlined },
  { key: '/orders', path: '/orders', title: '订单管理', icon: SnippetsOutlined },
  { key: '/finance', path: '/finance', title: '收付审核', icon: PayCircleOutlined },
  { key: '/reports', path: '/reports', title: 'BI 报表', icon: BarChartOutlined }
];

const hasPermission = (item: MenuNode) => {
  if (!auth.allowedMenuPaths.length) {
    return true;
  }
  const target = String(item.permissionPath || item.path || '');
  return target ? auth.allowedMenuPaths.includes(target) : true;
};

const filterMenuNodes = (nodes: MenuNode[]): MenuNode[] =>
  nodes
    .map((node) => {
      if (!node.children?.length) {
        return hasPermission(node) ? node : null;
      }
      const children = filterMenuNodes(node.children);
      if (!children.length) {
        return null;
      }
      return { ...node, children };
    })
    .filter(Boolean) as MenuNode[];

const menuItems = computed(() => filterMenuNodes(allMenuItems));

const menuLeafPaths = computed(() => {
  const result: string[] = [];
  const walk = (nodes: MenuNode[]) => {
    for (const node of nodes) {
      if (node.children?.length) {
        walk(node.children);
      } else if (node.path) {
        result.push(node.path);
      }
    }
  };
  walk(menuItems.value);
  return result;
});

const activeMenuKey = computed(() => {
  if (route.path === '/org') return '/system/org';
  if (route.path.startsWith('/orders/')) return '/orders';
  return route.path;
});

const openMenuKeys = computed(() => {
  if (collapsed.value) {
    return [];
  }
  if (activeMenuKey.value.startsWith('/system/')) {
    return ['system'];
  }
  return [];
});

const pageTitle = computed(() => String(route.meta.title || '工作台'));
const unreadCount = computed(() => notifications.value.filter((n) => !n.readFlag).length);
const userInitial = computed(() => (auth.profile?.username?.[0] || 'U').toUpperCase());

const onMenuClick = ({ key }: { key: string }) => {
  router.push(key);
};

const loadProfile = async () => {
  const { data } = await authApi.me();
  if (data.success) {
    auth.setProfile(data.data);
  }
};

const loadPermissions = async () => {
  const { data } = await permissionApi.menus();
  const paths = Array.from(
    new Set(
      (data.data || [])
        .map((item: any) => String(item.menuPath || '').trim())
        .filter((path: string) => path.startsWith('/'))
    )
  );
  auth.setAllowedMenuPaths(paths);

  const currentPermissionPath = String(route.meta.permissionPath || route.path);
  if (paths.length > 0 && !paths.includes(currentPermissionPath)) {
    await router.replace(menuLeafPaths.value[0] || '/dashboard');
  }
};

const loadNotifications = async () => {
  const { data } = await notificationApi.list();
  notifications.value = data.data || [];
};

const openNotifications = async () => {
  notificationOpen.value = true;
  await loadNotifications();
};

const markRead = async (id: number) => {
  await notificationApi.read(id);
  await loadNotifications();
};

const logout = () => {
  auth.logout();
  router.replace('/login');
};

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


