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
    allowedMenuPaths: [] as string[]
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
    logout() {
      this.token = '';
      this.profile = null;
      this.allowedMenuPaths = [];
      localStorage.removeItem('crm_token');
    }
  }
});
