import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '../stores/auth';

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

router.beforeEach((to) => {
  const auth = useAuthStore();
  if (to.path !== '/login' && !auth.isLogin) return '/login';
  if (to.path === '/login' && auth.isLogin) return '/dashboard';

  // 个人中心页面不需要权限检查
  if (to.path === '/profile') {
    return true;
  }

  if (to.path !== '/login' && auth.allowedMenuPaths.length > 0) {
    const permissionPath = String(to.meta.permissionPath || to.path);
    if (!auth.allowedMenuPaths.includes(permissionPath)) {
      return auth.allowedMenuPaths[0] || '/dashboard';
    }
  }
  return true;
});

export default router;
