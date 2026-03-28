import { useAuthStore } from '../stores/auth';

/**
 * 检查用户是否拥有指定按钮权限
 * @param permissionCode 权限code，如 'BTN_ORDER_CREATE'
 * @returns boolean
 */
export const hasButtonPermission = (permissionCode: string): boolean => {
  const auth = useAuthStore();

  // 未登录或无权限数据，拒绝访问
  if (!auth.profile || !auth.buttonPermissions) {
    return false;
  }

  // ADMIN 角色拥有所有权限
  if (auth.profile.roles?.includes('ADMIN')) {
    return true;
  }

  const normalized = permissionCode.trim().toUpperCase();
  return auth.buttonPermissions.includes(normalized);
};

/**
 * 检查用户是否拥有任一指定权限
 */
export const hasAnyButtonPermission = (permissionCodes: string[]): boolean => {
  return permissionCodes.some(code => hasButtonPermission(code));
};

/**
 * 检查用户是否拥有所有指定权限
 */
export const hasAllButtonPermissions = (permissionCodes: string[]): boolean => {
  return permissionCodes.every(code => hasButtonPermission(code));
};