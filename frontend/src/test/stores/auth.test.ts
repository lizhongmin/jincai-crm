import { beforeEach, describe, expect, it } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useAuthStore } from '../../stores/auth';

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    localStorage.clear();
  });

  it('writes token to localStorage', () => {
    const store = useAuthStore();
    store.setToken('token-001');

    expect(store.token).toBe('token-001');
    expect(localStorage.getItem('crm_token')).toBe('token-001');
    expect(store.isLogin).toBe(true);
  });

  it('stores profile and permission snapshots', () => {
    const store = useAuthStore();
    store.setProfile({
      userId: 'u-1',
      username: 'admin',
      roles: ['ADMIN']
    });
    store.setAllowedMenuPaths(['/dashboard', '/customers']);
    store.setButtonPermissions(['BTN_ORDER_CREATE']);
    store.setAllPermissions([{ code: 'BTN_ORDER_CREATE' }]);

    expect(store.profile?.username).toBe('admin');
    expect(store.allowedMenuPaths).toEqual(['/dashboard', '/customers']);
    expect(store.buttonPermissions).toEqual(['BTN_ORDER_CREATE']);
    expect(store.allPermissions).toEqual([{ code: 'BTN_ORDER_CREATE' }]);
  });

  it('clears auth state on logout', () => {
    const store = useAuthStore();
    store.setToken('token-001');
    store.setProfile({
      userId: 'u-1',
      username: 'admin',
      roles: ['ADMIN']
    });
    store.setAllowedMenuPaths(['/dashboard']);
    store.setButtonPermissions(['BTN_ORDER_CREATE']);
    store.setAllPermissions([{ code: 'BTN_ORDER_CREATE' }]);

    store.logout();

    expect(store.token).toBe('');
    expect(store.profile).toBeNull();
    expect(store.allowedMenuPaths).toEqual([]);
    expect(store.buttonPermissions).toEqual([]);
    expect(store.allPermissions).toEqual([]);
    expect(localStorage.getItem('crm_token')).toBeNull();
    expect(store.isLogin).toBe(false);
  });
});
