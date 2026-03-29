import { beforeEach, describe, expect, it } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useAuthStore } from '../../stores/auth';
import { hasAllButtonPermissions, hasAnyButtonPermission, hasButtonPermission } from '../../utils/permission';

describe('permission utils', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    localStorage.clear();
  });

  it('returns false when profile is missing', () => {
    const auth = useAuthStore();
    auth.buttonPermissions = ['BTN_ORDER_CREATE'];

    expect(hasButtonPermission('BTN_ORDER_CREATE')).toBe(false);
  });

  it('returns true for admin role regardless of button permissions', () => {
    const auth = useAuthStore();
    auth.setProfile({
      userId: 'u-1',
      username: 'admin',
      roles: ['ADMIN']
    });
    auth.buttonPermissions = [];

    expect(hasButtonPermission('BTN_ANY_CODE')).toBe(true);
  });

  it('normalizes permission code before checking list', () => {
    const auth = useAuthStore();
    auth.setProfile({
      userId: 'u-2',
      username: 'operator',
      roles: ['SALES']
    });
    auth.buttonPermissions = ['BTN_ORDER_CREATE', 'BTN_ORDER_EDIT'];

    expect(hasButtonPermission(' btn_order_create ')).toBe(true);
    expect(hasButtonPermission('BTN_ORDER_DELETE')).toBe(false);
  });

  it('supports any/all permission checks', () => {
    const auth = useAuthStore();
    auth.setProfile({
      userId: 'u-3',
      username: 'reviewer',
      roles: ['FINANCE']
    });
    auth.buttonPermissions = ['BTN_ORDER_CREATE', 'BTN_ORDER_APPROVE'];

    expect(hasAnyButtonPermission(['BTN_ORDER_REJECT', 'BTN_ORDER_APPROVE'])).toBe(true);
    expect(hasAllButtonPermissions(['BTN_ORDER_CREATE', 'BTN_ORDER_APPROVE'])).toBe(true);
    expect(hasAllButtonPermissions(['BTN_ORDER_CREATE', 'BTN_ORDER_DELETE'])).toBe(false);
  });
});
