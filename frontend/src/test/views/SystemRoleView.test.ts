import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import SystemRoleView from '../../views/SystemRoleView.vue';
import { orgApi } from '../../api/crm';

// Mock the API calls
vi.mock('../../api/crm', () => ({
  orgApi: {
    roles: vi.fn(),
    usersPage: vi.fn(),
    rolePermissions: vi.fn(),
    permissionsTree: vi.fn(),
    updateRole: vi.fn(),
    createRole: vi.fn(),
    deleteRole: vi.fn(),
    grantRole: vi.fn()
  }
}));

describe('SystemRoleView', () => {
  // Helper to mount the component with proper setup
  const mountComponent = () => {
    return mount(SystemRoleView, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            initialState: {}
          })
        ]
      }
    });
  };

  beforeEach(() => {
    // Reset all mocks before each test
    vi.resetAllMocks();

    // Setup default mock responses
    (orgApi.roles as any).mockResolvedValue({ data: { data: [] } });
    (orgApi.usersPage as any).mockResolvedValue({ data: { data: { items: [], total: 0 } } });
    (orgApi.rolePermissions as any).mockResolvedValue({ data: { data: [] } });
    (orgApi.permissionsTree as any).mockResolvedValue({ data: { data: [] } });
    (orgApi.updateRole as any).mockResolvedValue({ data: {} });
    (orgApi.createRole as any).mockResolvedValue({ data: { data: { id: 1 } } });
    (orgApi.deleteRole as any).mockResolvedValue({ data: {} });
    (orgApi.grantRole as any).mockResolvedValue({ data: {} });
  });

  describe('Component Initialization', () => {
    it('mounts successfully', () => {
      const wrapper = mountComponent();
      expect(wrapper.exists()).toBe(true);
    });

    it('loads roles and permissions tree on mount', async () => {
      const mockRoles = [{ id: 1, name: 'Admin', code: 'ADMIN' }];
      const mockPermissions = [{ moduleCode: 'customer', moduleName: '客户管理', subMenus: [] }];

      (orgApi.roles as any).mockResolvedValueOnce({ data: { data: mockRoles } });
      (orgApi.permissionsTree as any).mockResolvedValueOnce({ data: { data: mockPermissions } });

      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();

      expect(orgApi.roles).toHaveBeenCalled();
      expect(orgApi.permissionsTree).toHaveBeenCalled();
    });

    it('handles API errors gracefully during initialization', async () => {
      (orgApi.roles as any).mockRejectedValueOnce(new Error('Network error'));

      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();

      // Component should still mount even with API errors
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('Role Management', () => {
    it('opens role modal when openRole is called', async () => {
      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      // Call openRole with no arguments (create new)
      vm.openRole();

      // Should set modal to visible
      expect(vm.roleModal).toBe(true);
      // Should clear form
      expect(vm.roleForm.id).toBeUndefined();
      expect(vm.roleForm.code).toBe('');
      expect(vm.roleForm.name).toBe('');
    });

    it('opens role modal with existing role data for editing', async () => {
      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      const role = { id: 1, code: 'ADMIN', name: '管理员', description: '系统管理员' };
      vm.openRole(role);

      expect(vm.roleModal).toBe(true);
      expect(vm.roleForm.id).toBe(1);
      expect(vm.roleForm.code).toBe('ADMIN');
      expect(vm.roleForm.name).toBe('管理员');
      expect(vm.roleForm.description).toBe('系统管理员');
    });

    it('identifies admin role correctly', () => {
      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      expect(vm.isAdminRole({ code: 'ADMIN' })).toBe(true);
      expect(vm.isAdminRole({ code: 'admin' })).toBe(true);
      expect(vm.isAdminRole({ code: 'USER' })).toBe(false);
      expect(vm.isAdminRole({})).toBe(false);
    });
  });

  describe('Permission Management', () => {
    it('detects when grant selection has changed', async () => {
      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      // Set original permissions
      vm.grantOriginalPermissionIds = [1, 2, 3];
      // Set current permissions to be different
      vm.grantPermissionIds = [1, 2, 4];

      // Should detect change
      expect(vm.grantChanged).toBe(true);
    });

    it('detects when grant selection is unchanged', async () => {
      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      // Set both to same values
      vm.grantOriginalPermissionIds = [1, 2, 3];
      vm.grantPermissionIds = [1, 2, 3];

      // Should detect no change
      expect(vm.grantChanged).toBe(false);
    });

    it('resets grant selection to original values', async () => {
      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      vm.grantPermissionIds = [1, 2, 4];
      vm.grantOriginalPermissionIds = [1, 2, 3];

      vm.resetGrantSelection();

      expect(vm.grantPermissionIds).toEqual([1, 2, 3]);
    });
  });

  describe('Role Selection', () => {
    it('selects a role when selectRole is called', () => {
      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      vm.selectRole(5);

      expect(vm.selectedRoleId).toBe(5);
    });

    it('computes active role correctly', async () => {
      const mockRoles = [
        { id: 1, name: 'Admin', code: 'ADMIN' },
        { id: 2, name: 'User', code: 'USER' }
      ];

      (orgApi.roles as any).mockResolvedValueOnce({ data: { data: mockRoles } });

      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      // Wait for component to load
      await wrapper.vm.$nextTick();

      // Select the first role
      vm.selectRole(1);

      // Should compute active role correctly
      expect(vm.activeRole).toEqual({ id: 1, name: 'Admin', code: 'ADMIN' });
    });
  });

  describe('Role CRUD Operations', () => {
    it('creates a new role successfully', async () => {
      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      // Set form data
      vm.roleForm.code = 'TEST_ROLE';
      vm.roleForm.name = '测试角色';
      vm.roleForm.description = '用于测试的角色';

      // Mock successful creation
      const mockCreatedRole = { data: { id: 5 } };
      (orgApi.createRole as any).mockResolvedValueOnce({ data: mockCreatedRole });

      // Call saveRole
      await vm.saveRole();

      // Should call createRole with correct payload
      expect(orgApi.createRole).toHaveBeenCalledWith({
        code: 'TEST_ROLE',
        name: '测试角色',
        description: '用于测试的角色'
      });

      // Should close modal
      expect(vm.roleModal).toBe(false);
    });

    it('updates existing role successfully', async () => {
      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      // Set form data with ID (indicates update)
      vm.roleForm.id = 3;
      vm.roleForm.code = 'UPDATED_ROLE';
      vm.roleForm.name = '更新的角色';
      vm.roleForm.description = '已更新的描述';

      // Call saveRole
      await vm.saveRole();

      // Should call updateRole with correct payload
      expect(orgApi.updateRole).toHaveBeenCalledWith(3, {
        code: 'UPDATED_ROLE',
        name: '更新的角色',
        description: '已更新的描述'
      });
    });

    it('validates required fields before saving', async () => {
      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      // Set empty form data
      vm.roleForm.code = '';
      vm.roleForm.name = '';

      // Call saveRole
      await vm.saveRole();

      // Should not call any API
      expect(orgApi.createRole).not.toHaveBeenCalled();
      expect(orgApi.updateRole).not.toHaveBeenCalled();
    });

    it('deletes a role successfully', async () => {
      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      const role = { id: 5, name: 'To Delete', code: 'DELETE_ME' };

      await vm.removeRole(role);

      // Should call deleteRole with correct ID
      expect(orgApi.deleteRole).toHaveBeenCalledWith(5);
    });
  });

  describe('Permission Granting', () => {
    it('saves role permissions successfully', async () => {
      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      // Set selected role and permissions
      vm.selectedRoleId = 3;
      vm.grantPermissionIds = [1, 2, 3];

      await vm.saveGrant();

      // Should call grantRole with correct parameters
      expect(orgApi.grantRole).toHaveBeenCalledWith(3, [1, 2, 3]);
    });

    it('does not save permissions when no role is selected', async () => {
      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      // No selected role
      vm.selectedRoleId = undefined;
      vm.grantPermissionIds = [1, 2, 3];

      await vm.saveGrant();

      // Should not call grantRole
      expect(orgApi.grantRole).not.toHaveBeenCalled();
    });
  });

  describe('Role Filtering', () => {
    it('filters roles by keyword', async () => {
      const mockRoles = [
        { id: 1, name: '系统管理员', code: 'ADMIN' },
        { id: 2, name: '销售经理', code: 'SALES_MANAGER' },
        { id: 3, name: '财务专员', code: 'FINANCE_STAFF' }
      ];

      (orgApi.roles as any).mockResolvedValueOnce({ data: { data: mockRoles } });

      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      // Wait for component to load
      await wrapper.vm.$nextTick();

      // Set keyword filter
      vm.roleKeyword = '销售';

      // Should filter correctly
      expect(vm.filteredRoles).toHaveLength(1);
      expect(vm.filteredRoles[0].name).toBe('销售经理');
    });

    it('returns all roles when no keyword is provided', async () => {
      const mockRoles = [
        { id: 1, name: '系统管理员', code: 'ADMIN' },
        { id: 2, name: '销售经理', code: 'SALES_MANAGER' }
      ];

      (orgApi.roles as any).mockResolvedValueOnce({ data: { data: mockRoles } });

      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      // Wait for component to load
      await wrapper.vm.$nextTick();

      // No keyword filter
      vm.roleKeyword = '';

      // Should return all roles
      expect(vm.filteredRoles).toHaveLength(2);
    });
  });

  describe('Member Pagination', () => {
    it('handles member page change correctly', () => {
      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      // Set initial pagination
      vm.memberPage = 1;
      vm.memberPageSize = 10;

      // Simulate page change
      vm.onMemberPageChange({ current: 2, pageSize: 20 });

      expect(vm.memberPage).toBe(2);
      expect(vm.memberPageSize).toBe(20);
    });

    it('defaults to page 1 and size 10 when not provided', () => {
      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      vm.onMemberPageChange({});

      expect(vm.memberPage).toBe(1);
      expect(vm.memberPageSize).toBe(10);
    });
  });

  describe('Active Module Count', () => {
    it('calculates active module count correctly', async () => {
      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      // Mock permission groups
      vm.permissionGroups = [
        {
          moduleCode: 'customer',
          moduleName: '客户管理',
          subMenus: [
            {
              code: 'customer:list',
              name: '客户列表',
              menuPermission: { id: 10 },
              actions: [{ id: 1 }, { id: 2 }]
            }
          ]
        }
      ];

      // Set granted permissions
      vm.grantPermissionIds = [1, 2]; // Only action permissions, not menu permission

      // Should count 1 active module
      expect(vm.activeModuleCount).toBe(1);
    });

    it('returns 0 when no permissions are granted', async () => {
      const wrapper = mountComponent();
      const vm = wrapper.vm as any;

      vm.permissionGroups = [
        {
          moduleCode: 'customer',
          moduleName: '客户管理',
          subMenus: [
            {
              code: 'customer:list',
              name: '客户列表',
              menuPermission: { id: 10 },
              actions: [{ id: 1 }, { id: 2 }]
            }
          ]
        }
      ];

      vm.grantPermissionIds = []; // No permissions granted

      expect(vm.activeModuleCount).toBe(0);
    });
  });
});
