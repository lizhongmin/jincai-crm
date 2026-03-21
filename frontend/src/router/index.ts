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
      { path: 'org', name: 'org', meta: { title: '组织权限' }, component: () => import('../views/OrgView.vue') },
      { path: 'customers', name: 'customers', meta: { title: '客户中心' }, component: () => import('../views/CustomerView.vue') },
      { path: 'products', name: 'products', meta: { title: '产品团期' }, component: () => import('../views/ProductView.vue') },
      { path: 'workflow', name: 'workflow', meta: { title: '流程模板' }, component: () => import('../views/WorkflowView.vue') },
      { path: 'orders', name: 'orders', meta: { title: '订单审批' }, component: () => import('../views/OrderView.vue') },
      { path: 'finance', name: 'finance', meta: { title: '收付审核' }, component: () => import('../views/FinanceView.vue') },
      { path: 'reports', name: 'reports', meta: { title: 'BI 报表' }, component: () => import('../views/ReportView.vue') }
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

  if (to.path !== '/login' && auth.allowedMenuPaths.length > 0 && !auth.allowedMenuPaths.includes(to.path)) {
    return auth.allowedMenuPaths[0] || '/dashboard';
  }
  return true;
});

export default router;