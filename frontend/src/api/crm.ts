import http from './http';
import type { ApiResponse, DashboardFunnel, PageResult } from './types';

export const authApi = {
  login: (payload: { username: string; password: string; captchaId?: string; captchaCode?: string }) =>
    http.post<ApiResponse<{ token: string; userId: string; username: string; fullName: string; roles: string[] }>>('/auth/login', payload),
  me: () => http.get<ApiResponse<any>>('/auth/me'),
  loginState: (username: string) => http.get<ApiResponse<{ captchaRequired: boolean; locked: boolean; lockSeconds: number }>>('/auth/login-state', { params: { username } }),
  captcha: (username: string) => http.get<ApiResponse<{ captchaId: string; imageBase64: string; expireSeconds: number }>>('/auth/captcha', { params: { username } }),
  changePassword: (payload: { oldPassword: string; newPassword: string; confirmPassword: string }) =>
    http.post<ApiResponse<void>>('/auth/change-password', payload)
};

export const permissionApi = {
  menus: () => http.get<ApiResponse<any[]>>('/permissions/menus')
};

export const orgApi = {
  departments: () => http.get<ApiResponse<any[]>>('/departments'),
  departmentsTree: () => http.get<ApiResponse<any[]>>('/departments/tree'),
  createDepartment: (payload: { name: string; parentId?: string; leaderUserId?: string }) =>
    http.post<ApiResponse<any>>('/departments', payload),
  updateDepartment: (id: string, payload: { name: string; parentId?: string; leaderUserId?: string }) =>
    http.put<ApiResponse<any>>(`/departments/${id}`, payload),
  deleteDepartment: (id: string) => http.delete<ApiResponse<void>>(`/departments/${id}`),
  users: () => http.get<ApiResponse<any[]>>('/users'),
  usersPage: (params: { page?: number; size?: number; keyword?: string; departmentId?: string; roleId?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/users/page', { params }),
  createUser: (payload: any) => http.post<ApiResponse<any>>('/users', payload),
  updateUser: (id: string, payload: any) => http.put<ApiResponse<any>>(`/users/${id}`, payload),
  updateUserStatus: (id: string, enabled: boolean) => http.post<ApiResponse<any>>(`/users/${id}/status`, { enabled }),
  resetUserPassword: (id: string, password?: string) => http.post<ApiResponse<void>>(`/users/${id}/reset-password`, password ? { password } : {}),
  deleteUser: (id: string) => http.delete<ApiResponse<void>>(`/users/${id}`),
  roles: () => http.get<ApiResponse<any[]>>('/roles'),
  rolesPage: (params: { page?: number; size?: number; keyword?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/roles/page', { params }),
  createRole: (payload: any) => http.post<ApiResponse<any>>('/roles', payload),
  updateRole: (id: string, payload: any) => http.put<ApiResponse<any>>(`/roles/${id}`, payload),
  deleteRole: (id: string) => http.delete<ApiResponse<void>>(`/roles/${id}`),
  rolePermissions: (roleId: string) => http.get<ApiResponse<string[]>>(`/roles/${roleId}/permissions`),
  permissions: () => http.get<ApiResponse<any[]>>('/permissions'),
  permissionsTree: () => http.get<ApiResponse<any[]>>('/permissions/tree'),
  permissionsTreeView: () => http.get<ApiResponse<any[]>>('/permissions/tree-view'),
  createPermission: (payload: any) => http.post<ApiResponse<any>>('/permissions', payload),
  updatePermission: (id: string, payload: any) => http.put<ApiResponse<any>>(`/permissions/${id}`, payload),
  deletePermission: (id: string) => http.delete<ApiResponse<void>>(`/permissions/${id}`),
  grantRole: (roleId: string, permissionIds: string[]) =>
    http.post<ApiResponse<void>>(`/roles/${roleId}/grant`, { permissionIds })
};

export const securityApi = {
  loginPolicy: () => http.get<ApiResponse<any>>('/security/login-policy'),
  updateLoginPolicy: (payload: any) => http.put<ApiResponse<any>>('/security/login-policy', payload)
};

export const customerApi = {
  customers: () => http.get<ApiResponse<any[]>>('/customers'),
  customersPage: (params: { page?: number; size?: number; keyword?: string; tab?: string; ownerScope?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/customers/page', { params }),
  ownerOptions: () => http.get<ApiResponse<any[]>>('/customers/owner-options'),
  createCustomer: (payload: any) => http.post<ApiResponse<any>>('/customers', payload),
  updateCustomer: (id: string, payload: any) => http.put<ApiResponse<any>>(`/customers/${id}`, payload),
  deleteCustomer: (id: string) => http.delete<ApiResponse<void>>(`/customers/${id}`),
  travelers: (customerId: string) =>
    http.get<ApiResponse<any[]>>(`/customers/${customerId}/travelers`),
  travelerList: (customerId?: string) =>
    http.get<ApiResponse<any[]>>('/travelers', { params: customerId ? { customerId } : {} }),
  createTraveler: (customerId: string, payload: any) =>
    http.post<ApiResponse<any>>(`/travelers`, payload, { params: { customerId } }),
  updateTraveler: (travelerId: string, payload: any) =>
    http.put<ApiResponse<any>>(`/travelers/${travelerId}`, payload),
  deleteTraveler: (travelerId: string) => http.delete<ApiResponse<void>>(`/travelers/${travelerId}`),
  importCustomers: (file: File) => {
    const form = new FormData();
    form.append('file', file);
    return http.post<ApiResponse<any>>('/customers/import', form);
  }
};

export const productApi = {
  routes: () => http.get<ApiResponse<any[]>>('/routes'),
  routePage: (params: { page?: number; size?: number; keyword?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/routes/page', { params }),
  createRoute: (payload: any) => http.post<ApiResponse<any>>('/routes', payload),
  updateRoute: (id: string, payload: any) => http.put<ApiResponse<any>>(`/routes/${id}`, payload),
  deleteRoute: (id: string) => http.delete<ApiResponse<void>>(`/routes/${id}`),
  routeOrderPolicy: (id: string) => http.get<ApiResponse<any>>(`/routes/${id}/order-policy`),
  updateRouteOrderPolicy: (id: string, payload: any) => http.put<ApiResponse<any>>(`/routes/${id}/order-policy`, payload),
  departures: (routeId?: string) =>
    http.get<ApiResponse<any[]>>('/departures', { params: routeId ? { routeId } : {} }),
  departurePage: (params: { page?: number; size?: number; routeId?: string; keyword?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/departures/page', { params }),
  createDeparture: (payload: any) => http.post<ApiResponse<any>>('/departures', payload),
  updateDeparture: (id: string, payload: any) => http.put<ApiResponse<any>>(`/departures/${id}`, payload),
  deleteDeparture: (id: string) => http.delete<ApiResponse<void>>(`/departures/${id}`),
  departureOrderPolicy: (id: string) => http.get<ApiResponse<any>>(`/departures/${id}/order-policy`),
  updateDepartureOrderPolicy: (id: string, payload: any) => http.put<ApiResponse<any>>(`/departures/${id}/order-policy`, payload),
  prices: (departureId: string) => http.get<ApiResponse<any[]>>(`/departures/${departureId}/prices`),
  createPrice: (departureId: string, payload: any) =>
    http.post<ApiResponse<any>>(`/departures/${departureId}/prices`, payload),
  updatePrice: (departureId: string, priceId: string, payload: any) =>
    http.put<ApiResponse<any>>(`/departures/${departureId}/prices/${priceId}`, payload),
  deletePrice: (departureId: string, priceId: string) =>
    http.delete<ApiResponse<void>>(`/departures/${departureId}/prices/${priceId}`)
};

export const workflowApi = {
  list: () => http.get<ApiResponse<any[]>>('/workflow/templates'),
  page: (params: { page?: number; size?: number; keyword?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/workflow/templates/page', { params }),
  contextOptions: () => http.get<ApiResponse<any>>('/workflow/templates/context-options'),
  create: (payload: any) => http.post<ApiResponse<any>>('/workflow/templates', payload),
  update: (id: string, payload: any) => http.put<ApiResponse<any>>(`/workflow/templates/${id}`, payload),
  remove: (id: string) => http.delete<ApiResponse<void>>(`/workflow/templates/${id}`)
};

export const orderApi = {
  list: () => http.get<ApiResponse<any[]>>('/orders'),
  page: (params: { page?: number; size?: number; keyword?: string; status?: string; customerId?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/orders/page', { params }),
  contextOptions: () => http.get<ApiResponse<any>>('/orders/context-options'),
  routeDepartures: (routeId: string) =>
    http.get<ApiResponse<any[]>>(`/orders/context-options/routes/${routeId}/departures`),
  customerTravelers: (customerId: string) =>
    http.get<ApiResponse<any[]>>(`/orders/context-options/customers/${customerId}/travelers`),
  departurePrices: (departureId: string) =>
    http.get<ApiResponse<any[]>>(`/orders/context-options/departures/${departureId}/prices`),
  detail: (id: string) => http.get<ApiResponse<any>>(`/orders/${id}`),
  quote: (payload: any) => http.post<ApiResponse<any>>('/orders/quote', payload),
  create: (payload: any) => http.post<ApiResponse<any>>('/orders', payload),
  update: (id: string, payload: any) => http.put<ApiResponse<any>>(`/orders/${id}`, payload),
  remove: (id: string) => http.delete<ApiResponse<void>>(`/orders/${id}`),
  action: (id: string, payload: { action: string; comment?: string; targetRoleCode?: string }) =>
    http.post<ApiResponse<any>>(`/orders/${id}/actions`, payload),
  submit: (id: string) => http.post<ApiResponse<any>>(`/orders/${id}/submit`),
  approve: (id: string, comment: string) =>
    http.post<ApiResponse<any>>(`/orders/${id}/approve`, { comment }),
  reject: (id: string, comment: string) =>
    http.post<ApiResponse<any>>(`/orders/${id}/reject`, { comment }),
  logs: (id: string) => http.get<ApiResponse<any[]>>(`/orders/${id}/logs`),
  importOrders: (file: File) => {
    const form = new FormData();
    form.append('file', file);
    return http.post<ApiResponse<any>>('/orders/import', form);
  }
};

export const fileApi = {
  list: (bizType: string, bizId: string) =>
    http.get<ApiResponse<any[]>>('/files', { params: { bizType, bizId } }),
  upload: (bizType: string, bizId: string, file: File) => {
    const form = new FormData();
    form.append('bizType', bizType);
    form.append('bizId', String(bizId));
    form.append('file', file);
    return http.post<ApiResponse<any>>('/files/upload', form);
  }
};

export const auditApi = {
  list: (entityType: string, entityId: string) =>
    http.get<ApiResponse<any[]>>('/audits', { params: { entityType, entityId } }),
  apiLogsPage: (params: { page?: number; size?: number }) =>
    http.get<ApiResponse<PageResult<any>>>('/audits/api-logs', { params })
};

export const financeApi = {
  orderOptions: () => http.get<ApiResponse<any[]>>('/finance/orders/options'),
  receivables: (orderId: string) => http.get<ApiResponse<any[]>>(`/orders/${orderId}/receivables`),
  payables: (orderId: string) => http.get<ApiResponse<any[]>>(`/orders/${orderId}/payables`),
  refunds: (orderId: string) => http.get<ApiResponse<any[]>>(`/orders/${orderId}/refunds`),
  receipts: (receivableId: string) => http.get<ApiResponse<any[]>>(`/receivables/${receivableId}/receipts`),
  payments: (payableId: string) => http.get<ApiResponse<any[]>>(`/payables/${payableId}/payments`),
  createReceivable: (orderId: string, payload: any) =>
    http.post<ApiResponse<any>>(`/orders/${orderId}/receivables`, payload),
  updateReceivable: (id: string, payload: any) => http.put<ApiResponse<any>>(`/receivables/${id}`, payload),
  deleteReceivable: (id: string) => http.delete<ApiResponse<void>>(`/receivables/${id}`),
  createReceipt: (payload: any) => http.post<ApiResponse<any>>('/receipts', payload),
  createRefund: (payload: any) => http.post<ApiResponse<any>>('/refunds', payload),
  updateRefund: (id: string, payload: any) => http.put<ApiResponse<any>>(`/refunds/${id}`, payload),
  deleteRefund: (id: string) => http.delete<ApiResponse<void>>(`/refunds/${id}`),
  createPayable: (payload: any) => http.post<ApiResponse<any>>('/payables', payload),
  updatePayable: (id: string, payload: any) => http.put<ApiResponse<any>>(`/payables/${id}`, payload),
  deletePayable: (id: string) => http.delete<ApiResponse<void>>(`/payables/${id}`),
  createPayment: (payload: any) => http.post<ApiResponse<any>>('/payments', payload),
  review: (id: string, payload: any) => http.post<ApiResponse<any>>(`/finance/${id}/review`, payload)
};

export const reportApi = {
  funnel: () => http.get<ApiResponse<DashboardFunnel>>('/reports/sales-funnel'),
  aging: () => http.get<ApiResponse<any>>('/reports/cashflow-aging'),
  profit: () => http.get<ApiResponse<any[]>>('/reports/profit'),
  exportFunnel: () => http.get('/reports/sales-funnel/export', { responseType: 'blob' }),
  exportAging: () => http.get('/reports/cashflow-aging/export', { responseType: 'blob' }),
  exportProfit: () => http.get('/reports/profit/export', { responseType: 'blob' })
};

export const notificationApi = {
  list: () => http.get<ApiResponse<any[]>>('/notifications'),
  read: (id: string) => http.post<ApiResponse<void>>(`/notifications/${id}/read`)
};
