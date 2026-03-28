import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '../stores/auth';

/**
 * 路由配置
 * 路由元信息 (meta):
 * - title: 页面标题（显示在导航栏）
 * - permissionPath: 权限检查路径（默认使用路由路径）
 *
 * 权限控制策略:
 * 1. 未登录用户只能访问 /login
 * 2. 已登录用户通过 allowedMenuPaths (从后端获取) 控制菜单访问权限
 * 3. 个人中心 (/profile) 对所有登录用户开放，不需要额外权限
 * 4. 无权限访问的页面自动重定向到第一个可用菜单或看板页面
 */
const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', meta: { title: '登录' }, component: () => import('../views/LoginView.vue') },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'dashboard', meta: { title: '经营看板' }, component: () => import('../views/DashboardView.vue') },
      { path: 'profile', name: 'profile', meta: { title: '个人中心' }, component: () => import('../views/ProfileView.vue') },
      {
        path: 'system',
        redirect: '/system/org',
        meta: { title: '系统管理' },
        children: [
          {
            path: 'org',
            name: 'system-org',
            meta: { title: '组织架构', permissionPath: '/system/org' },
            component: () => import('../views/SystemOrgView.vue')
          },
          {
            path: 'role',
            name: 'system-role',
            meta: { title: '角色权限', permissionPath: '/system/role' },
            component: () => import('../views/SystemRoleView.vue')
          },
          {
            path: 'permission',
            name: 'system-permission',
            meta: { title: '菜单权限', permissionPath: '/system/permission' },
            component: () => import('../views/SystemPermissionView.vue')
          },
          {
            path: 'security',
            name: 'system-security',
            meta: { title: '登录安全', permissionPath: '/system/security' },
            component: () => import('../views/SystemSecurityView.vue')
          },
          {
            path: 'audit',
            name: 'system-audit',
            meta: { title: '审计日志', permissionPath: '/system/audit' },
            component: () => import('../views/SystemAuditLogView.vue')
          }
        ]
      },
      { path: 'customers', name: 'customers', meta: { title: '客户管理' }, component: () => import('../views/CustomerView.vue') },
      { path: 'products', name: 'products', meta: { title: '产品团期' }, component: () => import('../views/ProductView.vue') },
      { path: 'workflow', name: 'workflow', meta: { title: '流程模板' }, component: () => import('../views/WorkflowView.vue') },
      { path: 'orders', name: 'orders', meta: { title: '订单管理', permissionPath: '/orders' }, component: () => import('../views/OrderView.vue') },
      { path: 'orders/:id', name: 'order-detail', meta: { title: '订单详情', permissionPath: '/orders' }, component: () => import('../views/OrderDetailView.vue') },
      { path: 'finance', name: 'finance', meta: { title: '收付审核' }, component: () => import('../views/FinanceView.vue') },
      { path: 'reports', name: 'reports', meta: { title: 'BI 报表' }, component: () => import('../views/ReportView.vue') },
      { path: 'org', redirect: '/system/org' }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

/**
 * 全局路由守卫
 * 职责:
 * 1. 检查登录状态
 * 2. 检查菜单权限
 * 3. 处理重定向逻辑
 */
router.beforeEach((to) => {
  const auth = useAuthStore();

  // 如果未登录且访问非登录页，重定向到登录页
  if (to.path !== '/login' && !auth.isLogin) return '/login';

  // 已登录用户访问登录页，重定向到仪表板
  if (to.path === '/login' && auth.isLogin) return '/dashboard';

  // 个人中心页面不需要权限检查，所有登录用户可访问
  if (to.path === '/profile') {
    return true;
  }

  // 检查菜单权限：用户必须有权限访问该路径
  if (to.path !== '/login' && auth.allowedMenuPaths.length > 0) {
    const permissionPath = String(to.meta.permissionPath || to.path);
    if (!auth.allowedMenuPaths.includes(permissionPath)) {
      // 无权限访问，重定向到第一个可用菜单或默认仪表板
      return auth.allowedMenuPaths[0] || '/dashboard';
    }
  }
  return true;
});

export default router;
