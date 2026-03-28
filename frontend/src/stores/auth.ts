import { defineStore } from 'pinia';

/**
 * 用户资料接口
 * 包含用户基本信息和权限相关信息
 */
interface UserProfile {
  userId: string;           // 用户ID
  username: string;         // 用户名
  roles: string[];          // 用户角色列表
  departmentId?: string;    // 所属部门ID（可选）
  dataScope?: string;       // 数据权限范围（可选）
}

/**
 * 认证状态管理 Store
 * 职责：
 * 1. 管理用户登录状态和认证令牌
 * 2. 存储用户资料和权限信息
 * 3. 处理登录/登出逻辑
 *
 * 权限模型：
 * - allowedMenuPaths: 用户可访问的菜单路径列表（用于路由权限控制）
 * - buttonPermissions: 用户拥有的按钮权限码列表（用于按钮级权限控制）
 * - allPermissions: 完整权限数据（用于复杂权限判断）
 */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('crm_token') || '',  // JWT 认证令牌
    profile: null as UserProfile | null,             // 用户资料
    allowedMenuPaths: [] as string[],                // 允许访问的菜单路径
    buttonPermissions: [] as string[],               // 按钮权限 code 列表
    allPermissions: [] as any[]                      // 完整权限数据
  }),

  /**
   * Getters 计算属性
   */
  getters: {
    /**
     * 检查用户是否已登录
     * @returns {boolean} 已登录返回 true，否则返回 false
     */
    isLogin: (state) => Boolean(state.token)
  },

  /**
   * Actions 状态变更方法
   */
  actions: {
    /**
     * 设置认证令牌并保存到本地存储
     * @param {string} token JWT 认证令牌
     */
    setToken(token: string) {
      this.token = token;
      localStorage.setItem('crm_token', token);
    },

    /**
     * 设置用户资料
     * @param {UserProfile} profile 用户资料对象
     */
    setProfile(profile: UserProfile) {
      this.profile = profile;
    },

    /**
     * 设置允许访问的菜单路径列表
     * @param {string[]} paths 菜单路径数组
     */
    setAllowedMenuPaths(paths: string[]) {
      this.allowedMenuPaths = paths;
    },

    /**
     * 设置按钮权限码列表
     * @param {string[]} codes 按钮权限码数组
     */
    setButtonPermissions(codes: string[]) {
      this.buttonPermissions = codes;
    },

    /**
     * 设置完整权限数据
     * @param {any[]} permissions 权限数据数组
     */
    setAllPermissions(permissions: any[]) {
      this.allPermissions = permissions;
    },

    /**
     * 用户登出操作
     * 清除所有认证相关信息
     */
    logout() {
      this.token = '';
      this.profile = null;
      this.allowedMenuPaths = [];
      this.buttonPermissions = [];
      this.allPermissions = [];
      localStorage.removeItem('crm_token');
    }
  }
});
