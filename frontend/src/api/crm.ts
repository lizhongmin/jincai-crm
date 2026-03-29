import http from './http';
import type { ApiResponse, DashboardFunnel, PageResult } from './types';

/**
 * 认证相关 API
 * 包含登录、用户信息获取、密码修改等功能
 */
export const authApi = {
  /**
   * 用户登录
   * @param payload 登录凭证
   * @returns 包含 JWT token 和用户信息的响应
   */
  login: (payload: { username: string; password: string; captchaId?: string; captchaCode?: string }) =>
    http.post<ApiResponse<{ token: string; userId: string; username: string; fullName: string; roles: string[] }>>('/auth/login', payload),

  /**
   * 获取当前用户信息
   * @returns 包含用户完整资料的响应
   */
  me: () => http.get<ApiResponse<any>>('/auth/me'),

  /**
   * 获取用户登录状态（用于检查是否需要验证码）
   * @param username 用户名
   * @returns 包含登录状态信息的响应
   */
  loginState: (username: string) => http.get<ApiResponse<{ captchaRequired: boolean; locked: boolean; lockSeconds: number }>>('/auth/login-state', { params: { username } }),

  /**
   * 获取登录验证码
   * @param username 用户名
   * @returns 包含验证码图片数据的响应
   */
  captcha: (username: string) => http.get<ApiResponse<{ captchaId: string; imageBase64: string; expireSeconds: number }>>('/auth/captcha', { params: { username } }),

  /**
   * 修改密码
   * @param payload 密码修改信息
   * @returns 操作结果响应
   */
  changePassword: (payload: { oldPassword: string; newPassword: string; confirmPassword: string }) =>
    http.post<ApiResponse<void>>('/auth/change-password', payload)
};

/**
 * 权限相关 API
 * 包含菜单权限获取等功能
 */
export const permissionApi = {
  /**
   * 获取当前用户可访问的菜单列表
   * @returns 包含菜单数据的响应
   */
  menus: () => http.get<ApiResponse<any[]>>('/permissions/menus')
};

/**
 * 组织架构相关 API
 * 包含部门、用户、角色、权限管理等功能
 */
export const orgApi = {
  // 部门管理
  departments: () => http.get<ApiResponse<any[]>>('/departments'),
  departmentsTree: () => http.get<ApiResponse<any[]>>('/departments/tree'),
  createDepartment: (payload: { name: string; parentId?: string; leaderUserId?: string }) =>
    http.post<ApiResponse<any>>('/departments', payload),
  updateDepartment: (id: string, payload: { name: string; parentId?: string; leaderUserId?: string }) =>
    http.put<ApiResponse<any>>(`/departments/${id}`, payload),
  deleteDepartment: (id: string) => http.delete<ApiResponse<void>>(`/departments/${id}`),

  // 用户管理
  users: () => http.get<ApiResponse<any[]>>('/users'),
  usersPage: (params: { page?: number; size?: number; keyword?: string; departmentId?: string; roleId?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/users/page', { params }),
  createUser: (payload: any) => http.post<ApiResponse<any>>('/users', payload),
  updateUser: (id: string, payload: any) => http.put<ApiResponse<any>>(`/users/${id}`, payload),
  updateUserStatus: (id: string, enabled: boolean) => http.post<ApiResponse<any>>(`/users/${id}/status`, { enabled }),
  resetUserPassword: (id: string, password?: string) => http.post<ApiResponse<void>>(`/users/${id}/reset-password`, password ? { password } : {}),
  deleteUser: (id: string) => http.delete<ApiResponse<void>>(`/users/${id}`),

  // 角色管理
  roles: () => http.get<ApiResponse<any[]>>('/roles'),
  rolesPage: (params: { page?: number; size?: number; keyword?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/roles/page', { params }),
  createRole: (payload: any) => http.post<ApiResponse<any>>('/roles', payload),
  updateRole: (id: string, payload: any) => http.put<ApiResponse<any>>(`/roles/${id}`, payload),
  deleteRole: (id: string) => http.delete<ApiResponse<void>>(`/roles/${id}`),
  rolePermissions: (roleId: string) => http.get<ApiResponse<string[]>>(`/roles/${roleId}/permissions`),

  // 权限管理
  permissions: () => http.get<ApiResponse<any[]>>('/permissions'),
  permissionsTree: () => http.get<ApiResponse<any[]>>('/permissions/tree'),
  permissionsTreeView: () => http.get<ApiResponse<any[]>>('/permissions/tree-view'),
  createPermission: (payload: any) => http.post<ApiResponse<any>>('/permissions', payload),
  updatePermission: (id: string, payload: any) => http.put<ApiResponse<any>>(`/permissions/${id}`, payload),
  deletePermission: (id: string) => http.delete<ApiResponse<void>>(`/permissions/${id}`),
  grantRole: (roleId: string, permissionIds: string[]) =>
    http.post<ApiResponse<void>>(`/roles/${roleId}/grant`, { permissionIds })
};

/**
 * 安全策略相关 API
 * 包含登录安全策略配置等功能
 */
export const securityApi = {
  /**
   * 获取登录安全策略
   * @returns 包含安全策略配置的响应
   */
  loginPolicy: () => http.get<ApiResponse<any>>('/security/login-policy'),

  /**
   * 更新登录安全策略
   * @param payload 安全策略配置
   * @returns 操作结果响应
   */
  updateLoginPolicy: (payload: any) => http.put<ApiResponse<any>>('/security/login-policy', payload)
};

/**
 * 客户管理相关 API
 * 包含客户、出行人管理等功能
 */
export const customerApi = {
  // 客户管理
  customers: () => http.get<ApiResponse<any[]>>('/customers'),
  customersPage: (params: { page?: number; size?: number; keyword?: string; tab?: string; ownerScope?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/customers/page', { params }),
  ownerOptions: () => http.get<ApiResponse<any[]>>('/customers/owner-options'),
  createCustomer: (payload: any) => http.post<ApiResponse<any>>('/customers', payload),
  updateCustomer: (id: string, payload: any) => http.put<ApiResponse<any>>(`/customers/${id}`, payload),
  deleteCustomer: (id: string) => http.delete<ApiResponse<void>>(`/customers/${id}`),

  // 出行人管理
  travelerPage: (params: { page?: number; size?: number; keyword?: string; customerId?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/travelers/page', { params }),
  travelers: (customerId: string) =>
    http.get<ApiResponse<any[]>>(`/customers/${customerId}/travelers`),
  travelerList: (customerId?: string) =>
    http.get<ApiResponse<any[]>>('/travelers', { params: customerId ? { customerId } : {} }),
  createTraveler: (customerId: string, payload: any) =>
    http.post<ApiResponse<any>>(`/travelers`, payload, { params: { customerId } }),
  updateTraveler: (travelerId: string, payload: any) =>
    http.put<ApiResponse<any>>(`/travelers/${travelerId}`, payload),
  deleteTraveler: (travelerId: string) => http.delete<ApiResponse<void>>(`/travelers/${travelerId}`),

  /**
   * 导入客户数据
   * @param file CSV 文件
   * @returns 导入结果响应
   */
  importCustomers: (file: File) => {
    const form = new FormData();
    form.append('file', file);
    return http.post<ApiResponse<any>>('/customers/import', form);
  }
};

/**
 * 产品管理相关 API
 * 包含线路、团期、价格管理等功能
 */
export const productApi = {
  // 线路管理
  routes: () => http.get<ApiResponse<any[]>>('/routes'),
  routePage: (params: { page?: number; size?: number; keyword?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/routes/page', { params }),
  createRoute: (payload: any) => http.post<ApiResponse<any>>('/routes', payload),
  updateRoute: (id: string, payload: any) => http.put<ApiResponse<any>>(`/routes/${id}`, payload),
  deleteRoute: (id: string) => http.delete<ApiResponse<void>>(`/routes/${id}`),
  routeOrderPolicy: (id: string) => http.get<ApiResponse<any>>(`/routes/${id}/order-policy`),
  updateRouteOrderPolicy: (id: string, payload: any) => http.put<ApiResponse<any>>(`/routes/${id}/order-policy`, payload),

  // 团期管理
  departures: (routeId?: string) =>
    http.get<ApiResponse<any[]>>('/departures', { params: routeId ? { routeId } : {} }),
  departurePage: (params: { page?: number; size?: number; routeId?: string; keyword?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/departures/page', { params }),
  createDeparture: (payload: any) => http.post<ApiResponse<any>>('/departures', payload),
  updateDeparture: (id: string, payload: any) => http.put<ApiResponse<any>>(`/departures/${id}`, payload),
  deleteDeparture: (id: string) => http.delete<ApiResponse<void>>(`/departures/${id}`),
  departureOrderPolicy: (id: string) => http.get<ApiResponse<any>>(`/departures/${id}/order-policy`),
  updateDepartureOrderPolicy: (id: string, payload: any) => http.put<ApiResponse<any>>(`/departures/${id}/order-policy`, payload),

  // 价格管理
  prices: (departureId: string) => http.get<ApiResponse<any[]>>(`/departures/${departureId}/prices`),
  createPrice: (departureId: string, payload: any) =>
    http.post<ApiResponse<any>>(`/departures/${departureId}/prices`, payload),
  updatePrice: (departureId: string, priceId: string, payload: any) =>
    http.put<ApiResponse<any>>(`/departures/${departureId}/prices/${priceId}`, payload),
  deletePrice: (departureId: string, priceId: string) =>
    http.delete<ApiResponse<void>>(`/departures/${departureId}/prices/${priceId}`)
};

/**
 * 工作流相关 API
 * 包含流程模板管理等功能
 */
export const workflowApi = {
  /**
   * 获取流程模板列表
   * @returns 包含模板数据的响应
   */
  list: () => http.get<ApiResponse<any[]>>('/workflow/templates'),

  /**
   * 分页获取流程模板列表
   * @param params 分页和搜索参数
   * @returns 包含分页模板数据的响应
   */
  page: (params: { page?: number; size?: number; keyword?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/workflow/templates/page', { params }),

  /**
   * 获取流程模板上下文选项
   * @returns 包含上下文选项的响应
   */
  contextOptions: () => http.get<ApiResponse<any>>('/workflow/templates/context-options'),

  /**
   * 创建流程模板
   * @param payload 模板数据
   * @returns 创建结果响应
   */
  create: (payload: any) => http.post<ApiResponse<any>>('/workflow/templates', payload),

  /**
   * 更新流程模板
   * @param id 模板ID
   * @param payload 模板数据
   * @returns 更新结果响应
   */
  update: (id: string, payload: any) => http.put<ApiResponse<any>>(`/workflow/templates/${id}`, payload),

  /**
   * 删除流程模板
   * @param id 模板ID
   * @returns 删除结果响应
   */
  remove: (id: string) => http.delete<ApiResponse<void>>(`/workflow/templates/${id}`)
};

/**
 * 订单管理相关 API
 * 包含订单创建、审批、状态变更等功能
 */
export const orderApi = {
  // 订单查询
  list: () => http.get<ApiResponse<any[]>>('/orders'),
  page: (params: { page?: number; size?: number; keyword?: string; status?: string; customerId?: string; ownerUserId?: string; departmentId?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/orders/page', { params }),
  /**
   * 获取订单上下文选项（用于表单选择器）
   * 注意：为避免数据量过大，建议实现分页或搜索加载
   * @deprecated 建议使用分页接口替代
   */
  contextOptions: () => http.get<ApiResponse<any>>('/orders/context-options'),
  routeDepartures: (routeId: string) =>
    http.get<ApiResponse<any[]>>(`/orders/context-options/routes/${routeId}/departures`),
  customerTravelers: (customerId: string) =>
    http.get<ApiResponse<any[]>>(`/orders/context-options/customers/${customerId}/travelers`),
  departurePrices: (departureId: string) =>
    http.get<ApiResponse<any[]>>(`/orders/context-options/departures/${departureId}/prices`),

  // 订单操作
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

  /**
   * 导入订单数据
   * @param file CSV 文件
   * @returns 导入结果响应
   */
  importOrders: (file: File) => {
    const form = new FormData();
    form.append('file', file);
    return http.post<ApiResponse<any>>('/orders/import', form);
  }
};

/**
 * 文件管理相关 API
 * 包含文件上传、下载、列表等功能
 */
export const fileApi = {
  /**
   * 获取文件列表
   * @param bizType 业务类型
   * @param bizId 业务ID
   * @returns 包含文件列表的响应
   */
  list: (bizType: string, bizId: string) =>
    http.get<ApiResponse<any[]>>('/files', { params: { bizType, bizId } }),

  /**
   * 上传文件
   * @param bizType 业务类型
   * @param bizId 业务ID
   * @param file 文件对象
   * @returns 上传结果响应
   */
  upload: (bizType: string, bizId: string, file: File) => {
    const form = new FormData();
    form.append('bizType', bizType);
    form.append('bizId', String(bizId));
    form.append('file', file);
    return http.post<ApiResponse<any>>('/files/upload', form);
  }
};

/**
 * 审计日志相关 API
 * 包含操作日志、API日志查询等功能
 */
export const auditApi = {
  /**
   * 获取实体审计日志列表
   * @param entityType 实体类型
   * @param entityId 实体ID
   * @returns 包含审计日志列表的响应
   */
  list: (entityType: string, entityId: string) =>
    http.get<ApiResponse<any[]>>('/audits', { params: { entityType, entityId } }),

  /**
   * 分页获取API日志列表
   * @param params 分页参数
   * @returns 包含分页API日志数据的响应
   */
  apiLogsPage: (params: { page?: number; size?: number; keyword?: string; startTime?: string; endTime?: string }) =>
    http.get<ApiResponse<PageResult<any>>>('/audits/api-logs', { params })
};

/**
 * 财务管理相关 API
 * 包含应收、应付、退款、收付款管理等功能
 */
export const financeApi = {
  // 基础数据
  orderOptions: () => http.get<ApiResponse<any[]>>('/finance/orders/options'),

  // 应收管理
  receivables: (orderId: string) => http.get<ApiResponse<any[]>>(`/orders/${orderId}/receivables`),
  receipts: (receivableId: string) => http.get<ApiResponse<any[]>>(`/receivables/${receivableId}/receipts`),
  createReceivable: (orderId: string, payload: any) =>
    http.post<ApiResponse<any>>(`/orders/${orderId}/receivables`, payload),
  updateReceivable: (id: string, payload: any) => http.put<ApiResponse<any>>(`/receivables/${id}`, payload),
  deleteReceivable: (id: string) => http.delete<ApiResponse<void>>(`/receivables/${id}`),
  createReceipt: (payload: any) => http.post<ApiResponse<any>>('/receipts', payload),

  // 退款管理
  refunds: (orderId: string) => http.get<ApiResponse<any[]>>(`/orders/${orderId}/refunds`),
  createRefund: (payload: any) => http.post<ApiResponse<any>>('/refunds', payload),
  updateRefund: (id: string, payload: any) => http.put<ApiResponse<any>>(`/refunds/${id}`, payload),
  deleteRefund: (id: string) => http.delete<ApiResponse<void>>(`/refunds/${id}`),

  // 应付管理
  payables: (orderId: string) => http.get<ApiResponse<any[]>>(`/orders/${orderId}/payables`),
  payments: (payableId: string) => http.get<ApiResponse<any[]>>(`/payables/${payableId}/payments`),
  createPayable: (payload: any) => http.post<ApiResponse<any>>('/payables', payload),
  updatePayable: (id: string, payload: any) => http.put<ApiResponse<any>>(`/payables/${id}`, payload),
  deletePayable: (id: string) => http.delete<ApiResponse<void>>(`/payables/${id}`),
  createPayment: (payload: any) => http.post<ApiResponse<any>>('/payments', payload),

  // 财务审核
  review: (id: string, payload: any) => http.post<ApiResponse<any>>(`/finance/${id}/review`, payload)
};

/**
 * 报表相关 API
 * 包含销售漏斗、账龄分析、利润分析等报表功能
 */
export const reportApi = {
  /**
   * 获取销售漏斗数据
   * @returns 包含漏斗数据的响应
   */
  funnel: () => http.get<ApiResponse<DashboardFunnel>>('/reports/sales-funnel'),

  /**
   * 获取应收账龄分析数据
   * @returns 包含账龄分析数据的响应
   */
  aging: () => http.get<ApiResponse<any>>('/reports/cashflow-aging'),

  /**
   * 获取利润分析数据
   * @returns 包含利润分析数据的响应
   */
  profit: () => http.get<ApiResponse<any[]>>('/reports/profit'),

  /**
   * 导出销售漏斗数据
   * @returns 包含导出文件的响应
   */
  exportFunnel: () => http.get('/reports/sales-funnel/export', { responseType: 'blob' }),

  /**
   * 导出账龄分析数据
   * @returns 包含导出文件的响应
   */
  exportAging: () => http.get('/reports/cashflow-aging/export', { responseType: 'blob' }),

  /**
   * 导出利润分析数据
   * @returns 包含导出文件的响应
   */
  exportProfit: () => http.get('/reports/profit/export', { responseType: 'blob' })
};

/**
 * 通知相关 API
 * 包含系统通知获取和标记已读等功能
 */
export const notificationApi = {
  /**
   * 获取通知列表
   * @returns 包含通知列表的响应
   */
  list: () => http.get<ApiResponse<any[]>>('/notifications'),

  /**
   * 标记通知为已读
   * @param id 通知ID
   * @returns 操作结果响应
   */
  read: (id: string) => http.post<ApiResponse<void>>(`/notifications/${id}/read`)
};