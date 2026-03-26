package com.jincai.crm.test;

import com.jincai.crm.system.dto.PermissionTreeView;
import com.jincai.crm.system.entity.Permission;
import com.jincai.crm.system.service.PermissionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class PermissionTreeStructureFixTest {

    @Test
    public void testPermissionTreeBuilding() {
        // 创建测试数据
        List<Permission> testData = createTestPermissions();

        // 模拟PermissionService的treeView方法逻辑
        List<PermissionTreeView> tree = buildTreeView(testData);

        System.out.println("=== 修复后的权限树结构 ===");
        printTree(tree, 0);

        // 验证树结构
        validateTreeStructure(tree);
    }

    private List<Permission> createTestPermissions() {
        List<Permission> permissions = new ArrayList<>();

        // 顶级菜单
        permissions.add(createPermission(1L, "MENU_ORG", "系统管理", "MENU", "/system", null));
        permissions.add(createPermission(2L, "MENU_CUSTOMER", "客户管理", "MENU", "/customers", null));

        // 二级菜单（系统管理下）
        permissions.add(createPermission(3L, "MENU_ORG_DEPARTMENT", "部门管理", "MENU", "/system/org", 1L));
        permissions.add(createPermission(4L, "MENU_ORG_USER", "用户管理", "MENU", "/system/org", 1L));

        // 三级权限点（部门管理下）
        permissions.add(createPermission(5L, "BTN_ORG_DEPARTMENT_VIEW", "部门查看", "BUTTON", "", 3L));
        permissions.add(createPermission(6L, "BTN_ORG_DEPARTMENT_CREATE", "部门新增", "BUTTON", "", 3L));

        // 三级权限点（用户管理下）
        permissions.add(createPermission(7L, "BTN_ORG_USER_VIEW", "用户查看", "BUTTON", "", 4L));
        permissions.add(createPermission(8L, "BTN_ORG_USER_CREATE", "用户新增", "BUTTON", "", 4L));

        return permissions;
    }

    private Permission createPermission(Long id, String code, String name, String type, String menuPath, Long parentId) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setCode(code);
        permission.setName(name);
        permission.setType(type);
        permission.setMenuPath(menuPath);
        permission.setParentId(parentId);
        return permission;
    }

    private List<PermissionTreeView> buildTreeView(List<Permission> allPermissions) {
        // 先创建所有节点（不包含children）
        java.util.Map<Long, PermissionTreeView> nodeMap = new java.util.LinkedHashMap<>();
        allPermissions.forEach(permission -> nodeMap.put(permission.getId(), new PermissionTreeView(permission)));

        // 构建父子关系映射
        java.util.Map<Long, java.util.List<PermissionTreeView>> childrenRelation = new java.util.HashMap<>();
        for (PermissionTreeView node : nodeMap.values()) {
            if (node.parentId() != null) {
                childrenRelation.computeIfAbsent(node.parentId(), k -> new java.util.ArrayList<>()).add(node);
            }
        }

        // 重新构建带正确children的节点
        java.util.Map<Long, PermissionTreeView> finalNodeMap = new java.util.LinkedHashMap<>();
        for (PermissionTreeView node : nodeMap.values()) {
            java.util.List<PermissionTreeView> children = childrenRelation.getOrDefault(node.id(), new java.util.ArrayList<>());
            PermissionTreeView finalNode = new PermissionTreeView(
                node.id(), node.code(), node.name(), node.type(),
                node.menuPath(), node.parentId(), children
            );
            finalNodeMap.put(node.id(), finalNode);
        }

        // 找到所有根节点
        java.util.List<PermissionTreeView> roots = finalNodeMap.values().stream()
            .filter(node -> node.parentId() == null)
            .collect(java.util.stream.Collectors.toList());

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

        // 验证根节点数量
        assert tree.size() == 2 : "应该有两个根节点";

        // 找到系统管理节点
        PermissionTreeView systemMenu = tree.stream()
            .filter(node -> "系统管理".equals(node.name()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("未找到系统管理节点"));

        // 验证系统管理的子节点
        assert systemMenu.children() != null : "系统管理应该有子节点";
        assert systemMenu.children().size() == 2 : "系统管理应该有两个子菜单";

        PermissionTreeView deptMenu = systemMenu.children().get(0);
        PermissionTreeView userMenu = systemMenu.children().get(1);

        assert "部门管理".equals(deptMenu.name()) : "第一个子菜单应该是部门管理";
        assert "用户管理".equals(userMenu.name()) : "第二个子菜单应该是用户管理";

        // 验证部门管理的权限点
        assert deptMenu.children() != null : "部门管理应该有子节点";
        assert deptMenu.children().size() == 2 : "部门管理应该有两个权限点";

        PermissionTreeView deptView = deptMenu.children().get(0);
        PermissionTreeView deptCreate = deptMenu.children().get(1);

        assert "部门查看".equals(deptView.name()) : "第一个权限点应该是部门查看";
        assert "部门新增".equals(deptCreate.name()) : "第二个权限点应该是部门新增";

        // 验证用户管理的权限点
        assert userMenu.children() != null : "用户管理应该有子节点";
        assert userMenu.children().size() == 2 : "用户管理应该有两个权限点";

        PermissionTreeView userView = userMenu.children().get(0);
        PermissionTreeView userCreate = userMenu.children().get(1);

        assert "用户查看".equals(userView.name()) : "第一个权限点应该是用户查看";
        assert "用户新增".equals(userCreate.name()) : "第二个权限点应该是用户新增";

        System.out.println("✓ 树结构验证通过");
    }
}