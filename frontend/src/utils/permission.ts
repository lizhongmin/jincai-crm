import { useAuthStore } from '../stores/auth';

/**
 * 检查用户是否拥有指定按钮权限
 *
 * 权限检查逻辑：
 * 1. 未登录用户或无权限数据时，拒绝访问
 * 2. ADMIN 角色用户拥有所有权限
 * 3. 其他用户需在 buttonPermissions 列表中包含指定权限码
 *
 * @param permissionCode 权限code，如 'BTN_ORDER_CREATE'
 * @returns boolean 用户是否拥有该权限
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

  // 权限码标准化处理（去除空格并转为大写）
  const normalized = permissionCode.trim().toUpperCase();
  return auth.buttonPermissions.includes(normalized);
};

/**
 * 检查用户是否拥有任一指定权限
 *
 * 使用场景：当用户拥有多个可选权限中的任意一个时即可执行操作
 * 例如：用户可以是 'BTN_ORDER_CREATE' 或 'BTN_ORDER_EDIT' 权限的持有者
 *
 * @param permissionCodes 权限码数组
 * @returns boolean 用户是否拥有任一权限
 */
export const hasAnyButtonPermission = (permissionCodes: string[]): boolean => {
  return permissionCodes.some(code => hasButtonPermission(code));
};

/**
 * 检查用户是否拥有所有指定权限
 *
 * 使用场景：当用户必须拥有所有指定权限才能执行操作时
 * 例如：执行敏感操作需要同时拥有 'BTN_ORDER_APPROVE' 和 'BTN_ORDER_DELETE' 权限
 *
 * @param permissionCodes 权限码数组
 * @returns boolean 用户是否拥有所有权限
 */
export const hasAllButtonPermissions = (permissionCodes: string[]): boolean => {
  return permissionCodes.every(code => hasButtonPermission(code));
};