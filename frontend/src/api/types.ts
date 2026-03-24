export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface PageResult<T> {
  items: T[];
  total: number;
  page: number;
  size: number;
}

export interface LoginResponse {
  token: string;
  userId: number;
  username: string;
  fullName: string;
  roles: string[];
}

export interface DashboardFunnel {
  totalOrders: number;
  pendingApproval: number;
  approvedOrders: number;
  completedOrders: number;
  conversionRate: number;
}
