import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import PermissionTreePanel from '../../../components/org/PermissionTreePanel.vue';

// Test data factories
const makeAction = (id: number, name: string) => ({ id, name, type: 'BUTTON' });

const makeSubMenu = (code: string, name: string, actionIds: number[], menuPermId?: number) => ({
  code,
  name,
  menuPermission: menuPermId != null ? makeAction(menuPermId, `${name}-menu`) : null,
  actions: actionIds.map((id) => makeAction(id, `Action-${id}`))
});

const makeGroup = (
  moduleCode: string,
  moduleName: string,
  subMenus: ReturnType<typeof makeSubMenu>[],
  menuPermId?: number
) => ({
  moduleCode,
  moduleName,
  menuPermission: menuPermId != null ? makeAction(menuPermId, `${moduleName}-menu`) : null,
  subMenus
});

/** Helper: trigger a checkbox change by setting checked value and firing event */
async function triggerCheckbox(wrapper: ReturnType<typeof mount>, selector: string, checked: boolean) {
  const checkbox = wrapper.find(selector);
  const el = checkbox.element as HTMLInputElement;
  el.checked = checked;
  await checkbox.trigger('change');
}

async function triggerCheckboxAt(checkboxes: ReturnType<typeof mount>[], index: number, checked: boolean) {
  const el = checkboxes[index].element as HTMLInputElement;
  el.checked = checked;
  await checkboxes[index].trigger('change');
}

describe('PermissionTreePanel', () => {
  describe('Rendering', () => {
    it('renders nothing when groups is empty', () => {
      const wrapper = mount(PermissionTreePanel, {
        props: { groups: [], checkedKeys: [] }
      });
      expect(wrapper.find('.permission-panel').exists()).toBe(true);
      expect(wrapper.findAll('.module-card').length).toBe(0);
    });

    it('renders one card per group', () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [1, 2])]),
        makeGroup('order', '订单管理', [makeSubMenu('order:list', '订单列表', [3, 4])])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [] }
      });
      expect(wrapper.findAll('.module-card').length).toBe(2);
    });

    it('renders sub-menu rows within a group', () => {
      const groups = [
        makeGroup('customer', '客户管理', [
          makeSubMenu('customer:list', '客户列表', [1, 2]),
          makeSubMenu('customer:detail', '客户详情', [3])
        ])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [] }
      });
      expect(wrapper.findAll('.sub-menu-row').length).toBe(2);
    });

    it('shows "暂无权限点" when group has no submenus', () => {
      const groups = [makeGroup('misc', '其他', [])];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [] }
      });
      expect(wrapper.text()).toContain('暂无权限点');
    });

    it('shows "—" when submenu has no actions', () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [])])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [] }
      });
      expect(wrapper.find('.no-actions').text()).toBe('—');
    });
  });

  describe('Checkbox state: isGroupAllChecked', () => {
    it('returns false when no keys are checked', () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [1, 2])])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [] }
      });
      const checkboxes = wrapper.findAll('input[type="checkbox"]');
      // First checkbox is the group-level one
      expect(checkboxes[0].element.checked).toBe(false);
    });

    it('returns true when all keys in group are checked', () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [1, 2])])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [1, 2] }
      });
      const checkboxes = wrapper.findAll('input[type="checkbox"]');
      expect(checkboxes[0].element.checked).toBe(true);
    });

    it('returns false when group has no ids', () => {
      const groups = [makeGroup('empty', '空模块', [])];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [] }
      });
      const checkboxes = wrapper.findAll('input[type="checkbox"]');
      expect(checkboxes[0].element.checked).toBe(false);
    });
  });

  describe('Checkbox state: isGroupIndeterminate', () => {
    it('returns true when some but not all keys are checked', () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [1, 2, 3])])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [1] }
      });
      const groupCheckbox = wrapper.findAll('input[type="checkbox"]')[0];
      expect(groupCheckbox.element.indeterminate).toBe(true);
    });

    it('returns false when no keys are checked', () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [1, 2])])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [] }
      });
      const groupCheckbox = wrapper.findAll('input[type="checkbox"]')[0];
      expect(groupCheckbox.element.indeterminate).toBe(false);
    });

    it('returns false when all keys are checked', () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [1, 2])])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [1, 2] }
      });
      const groupCheckbox = wrapper.findAll('input[type="checkbox"]')[0];
      expect(groupCheckbox.element.indeterminate).toBe(false);
    });
  });

  describe('Checkbox state: isSubAllChecked', () => {
    it('returns true when all actions in sub are checked', () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [1, 2])])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [1, 2] }
      });
      // Second checkbox is the sub-level one
      const subCheckbox = wrapper.findAll('input[type="checkbox"]')[1];
      expect(subCheckbox.element.checked).toBe(true);
    });

    it('includes menuPermission id in sub all ids', () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [2, 3], 1)])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [1, 2, 3] }
      });
      const subCheckbox = wrapper.findAll('input[type="checkbox"]')[1];
      expect(subCheckbox.element.checked).toBe(true);
    });

    it('returns false when menuPermission is not checked', () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [2, 3], 1)])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [2, 3] } // menuPermission id=1 not checked
      });
      const subCheckbox = wrapper.findAll('input[type="checkbox"]')[1];
      expect(subCheckbox.element.checked).toBe(false);
    });
  });

  describe('toggleOne', () => {
    it('emits update:checkedKeys with id added when checked', async () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [1, 2])])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [] }
      });
      const actionCheckboxes = wrapper.findAll('.action-grid input[type="checkbox"]');
      await triggerCheckboxAt(actionCheckboxes as unknown as ReturnType<typeof mount>[], 0, true);
      const emitted = wrapper.emitted('update:checkedKeys');
      expect(emitted).toBeTruthy();
      expect(emitted![0][0]).toContain(1);
    });

    it('emits update:checkedKeys with id removed when unchecked', async () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [1, 2])])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [1, 2] }
      });
      const actionCheckboxes = wrapper.findAll('.action-grid input[type="checkbox"]');
      await triggerCheckboxAt(actionCheckboxes as unknown as ReturnType<typeof mount>[], 0, false);
      const emitted = wrapper.emitted('update:checkedKeys');
      expect(emitted).toBeTruthy();
      const newKeys = emitted![0][0] as number[];
      expect(newKeys).not.toContain(1);
      expect(newKeys).toContain(2);
    });
  });

  describe('toggleSub', () => {
    it('adds all sub ids when checked', async () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [1, 2, 3], 10)])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [] }
      });
      const subCheckboxes = wrapper.findAll('.sub-menu-header input[type="checkbox"]');
      await triggerCheckboxAt(subCheckboxes as unknown as ReturnType<typeof mount>[], 0, true);
      const emitted = wrapper.emitted('update:checkedKeys');
      expect(emitted).toBeTruthy();
      const newKeys = emitted![0][0] as number[];
      expect(newKeys).toContain(1);
      expect(newKeys).toContain(2);
      expect(newKeys).toContain(3);
      expect(newKeys).toContain(10); // menuPermission id
    });

    it('removes all sub ids when unchecked', async () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [1, 2, 3])])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [1, 2, 3] }
      });
      const subCheckboxes = wrapper.findAll('.sub-menu-header input[type="checkbox"]');
      await triggerCheckboxAt(subCheckboxes as unknown as ReturnType<typeof mount>[], 0, false);
      const emitted = wrapper.emitted('update:checkedKeys');
      expect(emitted).toBeTruthy();
      const newKeys = emitted![0][0] as number[];
      expect(newKeys).not.toContain(1);
      expect(newKeys).not.toContain(2);
      expect(newKeys).not.toContain(3);
    });

    it('preserves other sub ids when unchecking one sub', async () => {
      const groups = [
        makeGroup('customer', '客户管理', [
          makeSubMenu('customer:list', '客户列表', [1, 2]),
          makeSubMenu('customer:detail', '客户详情', [3, 4])
        ])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [1, 2, 3, 4] }
      });
      const subCheckboxes = wrapper.findAll('.sub-menu-header input[type="checkbox"]');
      await triggerCheckboxAt(subCheckboxes as unknown as ReturnType<typeof mount>[], 0, false);
      const emitted = wrapper.emitted('update:checkedKeys');
      expect(emitted).toBeTruthy();
      const newKeys = emitted![0][0] as number[];
      expect(newKeys).not.toContain(1);
      expect(newKeys).not.toContain(2);
      expect(newKeys).toContain(3);
      expect(newKeys).toContain(4);
    });
  });

  describe('toggleGroup', () => {
    it('adds all group ids when checked', async () => {
      const groups = [
        makeGroup(
          'customer',
          '客户管理',
          [
            makeSubMenu('customer:list', '客户列表', [1, 2]),
            makeSubMenu('customer:detail', '客户详情', [3])
          ],
          10
        )
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [] }
      });
      const groupCheckboxes = wrapper.findAll('.module-title input[type="checkbox"]');
      await triggerCheckboxAt(groupCheckboxes as unknown as ReturnType<typeof mount>[], 0, true);
      const emitted = wrapper.emitted('update:checkedKeys');
      expect(emitted).toBeTruthy();
      const newKeys = emitted![0][0] as number[];
      expect(newKeys).toContain(1);
      expect(newKeys).toContain(2);
      expect(newKeys).toContain(3);
      expect(newKeys).toContain(10); // group menuPermission id
    });

    it('removes all group ids when unchecked', async () => {
      const groups = [
        makeGroup('customer', '客户管理', [
          makeSubMenu('customer:list', '客户列表', [1, 2]),
          makeSubMenu('customer:detail', '客户详情', [3])
        ])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [1, 2, 3] }
      });
      const groupCheckboxes = wrapper.findAll('.module-title input[type="checkbox"]');
      await triggerCheckboxAt(groupCheckboxes as unknown as ReturnType<typeof mount>[], 0, false);
      const emitted = wrapper.emitted('update:checkedKeys');
      expect(emitted).toBeTruthy();
      const newKeys = emitted![0][0] as number[];
      expect(newKeys).not.toContain(1);
      expect(newKeys).not.toContain(2);
      expect(newKeys).not.toContain(3);
    });

    it('preserves other group ids when unchecking one group', async () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [1, 2])]),
        makeGroup('order', '订单管理', [makeSubMenu('order:list', '订单列表', [3, 4])])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [1, 2, 3, 4] }
      });
      const groupCheckboxes = wrapper.findAll('.module-title input[type="checkbox"]');
      await triggerCheckboxAt(groupCheckboxes as unknown as ReturnType<typeof mount>[], 0, false);
      const emitted = wrapper.emitted('update:checkedKeys');
      expect(emitted).toBeTruthy();
      const newKeys = emitted![0][0] as number[];
      expect(newKeys).not.toContain(1);
      expect(newKeys).not.toContain(2);
      expect(newKeys).toContain(3);
      expect(newKeys).toContain(4);
    });
  });

  describe('Edge cases', () => {
    it('handles null checkedKeys gracefully', () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [1, 2])])
      ];
      expect(() =>
        mount(PermissionTreePanel, {
          props: { groups, checkedKeys: null as unknown as number[] }
        })
      ).not.toThrow();
    });

    it('handles group with null menuPermission', () => {
      const groups = [
        {
          moduleCode: 'customer',
          moduleName: '客户管理',
          menuPermission: null,
          subMenus: [makeSubMenu('customer:list', '客户列表', [1, 2])]
        }
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [1, 2] }
      });
      const groupCheckbox = wrapper.findAll('.module-title input[type="checkbox"]')[0];
      expect(groupCheckbox.element.checked).toBe(true);
    });

    it('handles multiple groups and correctly tracks state per group', () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [1, 2])]),
        makeGroup('order', '订单管理', [makeSubMenu('order:list', '订单列表', [3, 4])])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [1, 2] } // only customer group checked
      });
      const groupCheckboxes = wrapper.findAll('.module-title input[type="checkbox"]');
      expect(groupCheckboxes[0].element.checked).toBe(true);
      expect(groupCheckboxes[1].element.checked).toBe(false);
    });

    it('does not emit duplicate ids when toggling group that already has some checked', async () => {
      const groups = [
        makeGroup('customer', '客户管理', [makeSubMenu('customer:list', '客户列表', [1, 2])])
      ];
      const wrapper = mount(PermissionTreePanel, {
        props: { groups, checkedKeys: [1] } // id 1 already checked
      });
      const groupCheckboxes = wrapper.findAll('.module-title input[type="checkbox"]');
      await triggerCheckboxAt(groupCheckboxes as unknown as ReturnType<typeof mount>[], 0, true);
      const emitted = wrapper.emitted('update:checkedKeys');
      expect(emitted).toBeTruthy();
      const newKeys = emitted![0][0] as number[];
      // Should not have duplicates
      const uniqueKeys = [...new Set(newKeys)];
      expect(newKeys.length).toBe(uniqueKeys.length);
      expect(newKeys).toContain(1);
      expect(newKeys).toContain(2);
    });
  });
});
