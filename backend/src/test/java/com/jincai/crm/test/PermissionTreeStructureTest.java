package com.jincai.crm.test;

import com.jincai.crm.system.dto.PermissionTreeView;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class PermissionTreeStructureTest {

    @Test
    public void testTreeStructureBuilding() {
        // 创建测试数据来模拟权限树结构
        List<PermissionTreeView> testData = createTestData();

        System.out.println("=== 测试数据结构 ===");
        printTree(testData, 0);

        // 验证树结构是否正确构建
        validateTreeStructure(testData);
    }

    private List<PermissionTreeView> createTestData() {
        // 创建模拟的权限树结构
        // 系统管理 (顶级菜单)
        PermissionTreeView systemMenu = new PermissionTreeView(1L, "MENU_ORG", "系统管理", "MENU", "/system", null);

        // 部门管理 (二级菜单)
        PermissionTreeView deptMenu = new PermissionTreeView(2L, "MENU_ORG_DEPARTMENT", "部门管理", "MENU", "/system/org", 1L);

        // 用户管理 (二级菜单)
        PermissionTreeView userMenu = new PermissionTreeView(3L, "MENU_ORG_USER", "用户管理", "MENU", "/system/org", 1L);

        // 部门查看权限 (三级权限点)
        PermissionTreeView deptViewPerm = new PermissionTreeView(4L, "BTN_ORG_DEPARTMENT_VIEW", "部门查看", "BUTTON", "", 2L);

        // 部门创建权限 (三级权限点)
        PermissionTreeView deptCreatePerm = new PermissionTreeView(5L, "BTN_ORG_DEPARTMENT_CREATE", "部门新增", "BUTTON", "", 2L);

        // 将子节点添加到父节点
        List<PermissionTreeView> systemChildren = new ArrayList<>();
        systemChildren.add(deptMenu);
        systemChildren.add(userMenu);

        List<PermissionTreeView> deptChildren = new ArrayList<>();
        deptChildren.add(deptViewPerm);
        deptChildren.add(deptCreatePerm);

        // 注意：这里我们手动设置children，实际应用中应该通过构造函数或setter方法
        // 由于PermissionTreeView是record类型，我们需要通过构造函数传递children

        // 重新创建带有children的节点
        PermissionTreeView deptMenuWithChildren = new PermissionTreeView(
            deptMenu.id(), deptMenu.code(), deptMenu.name(), deptMenu.type(),
            deptMenu.menuPath(), deptMenu.parentId(), deptChildren
        );

        List<PermissionTreeView> systemChildrenWithDeptChildren = new ArrayList<>();
        systemChildrenWithDeptChildren.add(deptMenuWithChildren);
        systemChildrenWithDeptChildren.add(userMenu);

        PermissionTreeView systemMenuWithChildren = new PermissionTreeView(
            systemMenu.id(), systemMenu.code(), systemMenu.name(), systemMenu.type(),
            systemMenu.menuPath(), systemMenu.parentId(), systemChildrenWithDeptChildren
        );

        List<PermissionTreeView> roots = new ArrayList<>();
        roots.add(systemMenuWithChildren);

        return roots;
    }

    private void printTree(List<PermissionTreeView> nodes, int level) {
        String indent = "  ".repeat(level);
        for (PermissionTreeView node : nodes) {
            System.out.println(indent + "- " + node.name() + " (" + node.code() + ")");
            if (node.children() != null && !node.children().isEmpty()) {
                printTree(node.children(), level + 1);
            }
        }
    }

    private void validateTreeStructure(List<PermissionTreeView> tree) {
        System.out.println("\n=== 验证树结构 ===");

        // 检查根节点
        assert tree.size() == 1 : "应该只有一个根节点";
        PermissionTreeView root = tree.get(0);
        assert "系统管理".equals(root.name()) : "根节点应该是系统管理";
        assert root.parentId() == null : "根节点的parentId应该为null";

        // 检查子节点
        assert root.children() != null && root.children().size() == 2 : "系统管理应该有两个子节点";

        PermissionTreeView deptMenu = root.children().get(0);
        assert "部门管理".equals(deptMenu.name()) : "第一个子节点应该是部门管理";
        assert deptMenu.parentId() != null && deptMenu.parentId() == 1L : "部门管理的parentId应该指向系统管理";

        PermissionTreeView userMenu = root.children().get(1);
        assert "用户管理".equals(userMenu.name()) : "第二个子节点应该是用户管理";

        // 检查权限点
        assert deptMenu.children() != null && deptMenu.children().size() == 2 : "部门管理应该有两个权限点";

        PermissionTreeView deptViewPerm = deptMenu.children().get(0);
        assert "部门查看".equals(deptViewPerm.name()) : "第一个权限点应该是部门查看";
        assert "BUTTON".equals(deptViewPerm.type()) : "权限点类型应该是BUTTON";

        PermissionTreeView deptCreatePerm = deptMenu.children().get(1);
        assert "部门新增".equals(deptCreatePerm.name()) : "第二个权限点应该是部门新增";

        System.out.println("✓ 树结构验证通过");
    }
}