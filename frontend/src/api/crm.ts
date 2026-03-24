import http from './http';
import type { ApiResponse, DashboardFunnel, PageResult } from './types';

export const authApi = {
  login: (payload: { username: string; password: string; captchaId?: string; captchaCode?: string }) =>
    http.post<ApiResponse<{ token: string; userId: number; username: string; fullName: string; roles: string[] }>>('/auth/login', payload),
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
  createDepartment: (payload: { name: string; parentId?: number; leaderUserId?: number }) =>
    http.post<ApiResponse<any>>('/departments', payload),
  updateDepartment: (id: number, payload: { name: string; parentId?: number; leaderUserId?: number }) =>
    http.put<ApiResponse<any>>(`/departments/${id}`, payload),
  deleteDepartment: (id: number) => http.delete<ApiResponse<void>>(`/departments/${id}`),
  users: () => http.get<ApiResponse<any[]>>('/users'),
  usersPage: (params: { page?: number; size?: number; keyword?: string; departmentId?: number; roleId?: number }) =>
    http.get<ApiResponse<PageResult<any>>>('/users/page', { params }),
  createUser: (payload: any) => http.post<ApiResponse<any>>('/users', payload),
  updateUser: (id: number, payload: any) => http.put<ApiResponse<any>>(`/users/${id}`, payload),
  updateUserStatus: (id: number, enabled: boolean) => http.post<ApiResponse<any>>(`/users/${id}/status`, { enabled }),
  resetUserPassword: (id: number, password?: string) => http.post<ApiResponse<void>>(`/users/${id}/reset-password`, password ? { password } : {}),
  deleteUser: (id: number) => http.delete<ApiResponse<void>>(`/users/${id}`),
  roles: () => http.get<ApiResponse<any[]>>('/roles'),
  rolesPage: (params: { page?: number; size?: number; keyword?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/roles/page', { params }),
  createRole: (payload: any) => http.post<ApiResponse<any>>('/roles', payload),
  updateRole: (id: number, payload: any) => http.put<ApiResponse<any>>(`/roles/${id}`, payload),
  deleteRole: (id: number) => http.delete<ApiResponse<void>>(`/roles/${id}`),
  rolePermissions: (roleId: number) => http.get<ApiResponse<number[]>>(`/roles/${roleId}/permissions`),
  permissions: () => http.get<ApiResponse<any[]>>('/permissions'),
  permissionsTree: () => http.get<ApiResponse<any[]>>('/permissions/tree'),
  grantRole: (roleId: number, permissionIds: number[]) =>
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
  updateCustomer: (id: number, payload: any) => http.put<ApiResponse<any>>(`/customers/${id}`, payload),
  deleteCustomer: (id: number) => http.delete<ApiResponse<void>>(`/customers/${id}`),
  travelers: (customerId: number) =>
    http.get<ApiResponse<any[]>>(`/customers/${customerId}/travelers`),
  travelerList: (customerId?: number) =>
    http.get<ApiResponse<any[]>>('/travelers', { params: customerId ? { customerId } : {} }),
  createTraveler: (customerId: number, payload: any) =>
    http.post<ApiResponse<any>>(`/customers/${customerId}/travelers`, payload),
  updateTraveler: (travelerId: number, payload: any) =>
    http.put<ApiResponse<any>>(`/customers/travelers/${travelerId}`, payload),
  deleteTraveler: (travelerId: number) => http.delete<ApiResponse<void>>(`/customers/travelers/${travelerId}`),
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
  updateRoute: (id: number, payload: any) => http.put<ApiResponse<any>>(`/routes/${id}`, payload),
  deleteRoute: (id: number) => http.delete<ApiResponse<void>>(`/routes/${id}`),
  routeOrderPolicy: (id: number) => http.get<ApiResponse<any>>(`/routes/${id}/order-policy`),
  updateRouteOrderPolicy: (id: number, payload: any) => http.put<ApiResponse<any>>(`/routes/${id}/order-policy`, payload),
  departures: (routeId?: number) =>
    http.get<ApiResponse<any[]>>('/departures', { params: routeId ? { routeId } : {} }),
  departurePage: (params: { page?: number; size?: number; routeId?: number; keyword?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/departures/page', { params }),
  createDeparture: (payload: any) => http.post<ApiResponse<any>>('/departures', payload),
  updateDeparture: (id: number, payload: any) => http.put<ApiResponse<any>>(`/departures/${id}`, payload),
  deleteDeparture: (id: number) => http.delete<ApiResponse<void>>(`/departures/${id}`),
  departureOrderPolicy: (id: number) => http.get<ApiResponse<any>>(`/departures/${id}/order-policy`),
  updateDepartureOrderPolicy: (id: number, payload: any) => http.put<ApiResponse<any>>(`/departures/${id}/order-policy`, payload),
  prices: (departureId: number) => http.get<ApiResponse<any[]>>(`/departures/${departureId}/prices`),
  createPrice: (departureId: number, payload: any) =>
    http.post<ApiResponse<any>>(`/departures/${departureId}/prices`, payload),
  updatePrice: (departureId: number, priceId: number, payload: any) =>
    http.put<ApiResponse<any>>(`/departures/${departureId}/prices/${priceId}`, payload),
  deletePrice: (departureId: number, priceId: number) =>
    http.delete<ApiResponse<void>>(`/departures/${departureId}/prices/${priceId}`)
};

export const workflowApi = {
  list: () => http.get<ApiResponse<any[]>>('/workflow/templates'),
  page: (params: { page?: number; size?: number; keyword?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/workflow/templates/page', { params }),
  contextOptions: () => http.get<ApiResponse<any>>('/workflow/templates/context-options'),
  create: (payload: any) => http.post<ApiResponse<any>>('/workflow/templates', payload),
  update: (id: number, payload: any) => http.put<ApiResponse<any>>(`/workflow/templates/${id}`, payload),
  remove: (id: number) => http.delete<ApiResponse<void>>(`/workflow/templates/${id}`)
};

export const orderApi = {
  list: () => http.get<ApiResponse<any[]>>('/orders'),
  page: (params: { page?: number; size?: number; keyword?: string; status?: string; customerId?: number }) =>
    http.get<ApiResponse<PageResult<any>>>('/orders/page', { params }),
  contextOptions: () => http.get<ApiResponse<any>>('/orders/context-options'),
  customerTravelers: (customerId: number) =>
    http.get<ApiResponse<any[]>>(`/orders/context-options/customers/${customerId}/travelers`),
  departurePrices: (departureId: number) =>
    http.get<ApiResponse<any[]>>(`/orders/context-options/departures/${departureId}/prices`),
  detail: (id: number) => http.get<ApiResponse<any>>(`/orders/${id}`),
  quote: (payload: any) => http.post<ApiResponse<any>>('/orders/quote', payload),
  create: (payload: any) => http.post<ApiResponse<any>>('/orders', payload),
  update: (id: number, payload: any) => http.put<ApiResponse<any>>(`/orders/${id}`, payload),
  remove: (id: number) => http.delete<ApiResponse<void>>(`/orders/${id}`),
  action: (id: number, payload: { action: string; comment?: string; targetRoleCode?: string }) =>
    http.post<ApiResponse<any>>(`/orders/${id}/actions`, payload),
  submit: (id: number) => http.post<ApiResponse<any>>(`/orders/${id}/submit`),
  approve: (id: number, comment: string) =>
    http.post<ApiResponse<any>>(`/orders/${id}/approve`, { comment }),
  reject: (id: number, comment: string) =>
    http.post<ApiResponse<any>>(`/orders/${id}/reject`, { comment }),
  logs: (id: number) => http.get<ApiResponse<any[]>>(`/orders/${id}/logs`),
  importOrders: (file: File) => {
    const form = new FormData();
    form.append('file', file);
    return http.post<ApiResponse<any>>('/orders/import', form);
  }
};

export const fileApi = {
  list: (bizType: string, bizId: number) =>
    http.get<ApiResponse<any[]>>('/files', { params: { bizType, bizId } }),
  upload: (bizType: string, bizId: number, file: File) => {
    const form = new FormData();
    form.append('bizType', bizType);
    form.append('bizId', String(bizId));
    form.append('file', file);
    return http.post<ApiResponse<any>>('/files/upload', form);
  }
};

export const auditApi = {
  list: (entityType: string, entityId: number) =>
    http.get<ApiResponse<any[]>>('/audits', { params: { entityType, entityId } })
};

export const financeApi = {
  orderOptions: () => http.get<ApiResponse<any[]>>('/finance/orders/options'),
  receivables: (orderId: number) => http.get<ApiResponse<any[]>>(`/orders/${orderId}/receivables`),
  payables: (orderId: number) => http.get<ApiResponse<any[]>>(`/orders/${orderId}/payables`),
  refunds: (orderId: number) => http.get<ApiResponse<any[]>>(`/orders/${orderId}/refunds`),
  receipts: (receivableId: number) => http.get<ApiResponse<any[]>>(`/receivables/${receivableId}/receipts`),
  payments: (payableId: number) => http.get<ApiResponse<any[]>>(`/payables/${payableId}/payments`),
  createReceivable: (orderId: number, payload: any) =>
    http.post<ApiResponse<any>>(`/orders/${orderId}/receivables`, payload),
  updateReceivable: (id: number, payload: any) => http.put<ApiResponse<any>>(`/receivables/${id}`, payload),
  deleteReceivable: (id: number) => http.delete<ApiResponse<void>>(`/receivables/${id}`),
  createReceipt: (payload: any) => http.post<ApiResponse<any>>('/receipts', payload),
  createRefund: (payload: any) => http.post<ApiResponse<any>>('/refunds', payload),
  updateRefund: (id: number, payload: any) => http.put<ApiResponse<any>>(`/refunds/${id}`, payload),
  deleteRefund: (id: number) => http.delete<ApiResponse<void>>(`/refunds/${id}`),
  createPayable: (payload: any) => http.post<ApiResponse<any>>('/payables', payload),
  updatePayable: (id: number, payload: any) => http.put<ApiResponse<any>>(`/payables/${id}`, payload),
  deletePayable: (id: number) => http.delete<ApiResponse<void>>(`/payables/${id}`),
  createPayment: (payload: any) => http.post<ApiResponse<any>>('/payments', payload),
  review: (id: number, payload: any) => http.post<ApiResponse<any>>(`/finance/${id}/review`, payload)
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
  read: (id: number) => http.post<ApiResponse<void>>(`/notifications/${id}/read`)
};
