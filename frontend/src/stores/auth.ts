import { defineStore } from 'pinia';

interface UserProfile {
  userId: string;
  username: string;
  roles: string[];
  departmentId?: string;
  dataScope?: string;
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('crm_token') || '',
    profile: null as UserProfile | null,
    allowedMenuPaths: [] as string[],
    buttonPermissions: [] as string[],  // 按钮权限 code
    allPermissions: [] as any[]          // 完整权限数据
  }),
  getters: {
    isLogin: (state) => Boolean(state.token)
  },
  actions: {
    setToken(token: string) {
      this.token = token;
      localStorage.setItem('crm_token', token);
    },
    setProfile(profile: UserProfile) {
      this.profile = profile;
    },
    setAllowedMenuPaths(paths: string[]) {
      this.allowedMenuPaths = paths;
    },
    setButtonPermissions(codes: string[]) {
      this.buttonPermissions = codes;
    },
    setAllPermissions(permissions: any[]) {
      this.allPermissions = permissions;
    },
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
