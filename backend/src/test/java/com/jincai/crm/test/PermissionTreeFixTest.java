package com.jincai.crm.test;

import org.junit.jupiter.api.Test;
import java.util.*;

public class PermissionTreeFixTest {

    // 模拟的权限实体类
    static class Permission {
        Long id;
        String code;
        String name;
        String type;
        String menuPath;
        Long parentId;

        Permission(Long id, String code, String name, String type, String menuPath, Long parentId) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.type = type;
            this.menuPath = menuPath;
            this.parentId = parentId;
        }
    }

    // 模拟的权限树视图类
    static class PermissionTreeView {
        Long id;
        String code;
        String name;
        String type;
        String menuPath;
        Long parentId;
        List<PermissionTreeView> children;

        PermissionTreeView(Long id, String code, String name, String type, String menuPath, Long parentId, List<PermissionTreeView> children) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.type = type;
            this.menuPath = menuPath;
            this.parentId = parentId;
            this.children = children;
        }

        @Override
        public String toString() {
            return name + " (" + code + ")";
        }
    }

    @Test
    public void testTreeViewBuilding() {
        // 模拟数据库中的权限数据
        List<Permission> allPermissions = Arrays.asList(
            // 顶级菜单
            new Permission(1L, "MENU_ORG", "系统管理", "MENU", "/system", null),

            // 二级菜单
            new Permission(2L, "MENU_ORG_DEPARTMENT", "部门管理", "MENU", "/system/org", 1L),
            new Permission(3L, "MENU_ORG_USER", "用户管理", "MENU", "/system/org", 1L),
            new Permission(4L, "MENU_ORG_ROLE", "角色管理", "MENU", "/system/role", 1L),
            new Permission(5L, "MENU_SECURITY", "登录安全", "MENU", "/security", null),

            // 三级权限点
            new Permission(6L, "BTN_ORG_DEPARTMENT_VIEW", "部门查看", "BUTTON", "", 2L),
            new Permission(7L, "BTN_ORG_DEPARTMENT_CREATE", "部门新增", "BUTTON", "", 2L),
            new Permission(8L, "BTN_ORG_USER_VIEW", "用户查看", "BUTTON", "", 3L),
            new Permission(9L, "BTN_SECURITY_POLICY_VIEW", "策略查看", "BUTTON", "", 5L)
        );

        System.out.println("=== 模拟权限树构建 ===");

        // 使用修复后的逻辑构建树
        List<PermissionTreeView> tree = buildTreeView(allPermissions);

        // 打印树结构
        printTree(tree, 0);

        // 验证
        assert tree.size() == 2 : "应该有两个根节点（系统管理和登录安全）";

        // 找到系统管理节点
        PermissionTreeView systemMenu = tree.stream()
            .filter(node -> "系统管理".equals(node.name))
            .findFirst()
            .orElseThrow(() -> new AssertionError("未找到系统管理节点"));

        assert systemMenu.children.size() == 4 : "系统管理应该有4个子节点";
        assert systemMenu.children.get(0).name.equals("部门管理") : "第一个子节点应该是部门管理";
        assert systemMenu.children.get(1).name.equals("用户管理") : "第二个子节点应该是用户管理";
        assert systemMenu.children.get(2).name.equals("角色管理") : "第三个子节点应该是角色管理";

        // 验证部门管理节点的子节点
        PermissionTreeView deptMenu = systemMenu.children.get(0);
        assert deptMenu.children.size() == 2 : "部门管理应该有2个权限点";
        assert deptMenu.children.get(0).name.equals("部门查看") : "第一个权限点应该是部门查看";
        assert deptMenu.children.get(1).name.equals("部门新增") : "第二个权限点应该是部门新增";

        System.out.println("\n✓ 权限树结构验证通过");
    }

    private List<PermissionTreeView> buildTreeView(List<Permission> allPermissions) {
        // 创建ID到Permission的映射
        Map<Long, Permission> permissionMap = new LinkedHashMap<>();
        for (Permission p : allPermissions) {
            permissionMap.put(p.id, p);
        }

        // 创建ID到PermissionTreeView的映射
        Map<Long, PermissionTreeView> nodeMap = new LinkedHashMap<>();
        for (Permission p : allPermissions) {
            nodeMap.put(p.id, new PermissionTreeView(
                p.id, p.code, p.name, p.type, p.menuPath, p.parentId, new ArrayList<>()
            ));
        }

        // 构建父子关系映射
        Map<Long, List<PermissionTreeView>> childrenRelation = new HashMap<>();
        for (PermissionTreeView node : nodeMap.values()) {
            if (node.parentId != null) {
                childrenRelation.computeIfAbsent(node.parentId, k -> new ArrayList<>()).add(node);
            }
        }

        // 重新构建带正确children的节点
        Map<Long, PermissionTreeView> finalNodeMap = new LinkedHashMap<>();
        for (PermissionTreeView node : nodeMap.values()) {
            List<PermissionTreeView> children = childrenRelation.getOrDefault(node.id, new ArrayList<>());
            PermissionTreeView finalNode = new PermissionTreeView(
                node.id, node.code, node.name, node.type,
                node.menuPath, node.parentId, children
            );
            finalNodeMap.put(node.id, finalNode);
        }

        // 找到所有根节点
        List<PermissionTreeView> roots = new ArrayList<>();
        for (PermissionTreeView node : finalNodeMap.values()) {
            if (node.parentId == null) {
                roots.add(node);
            }
        }

        return roots;
    }

    private void printTree(List<PermissionTreeView> nodes, int level) {
        String indent = "  ".repeat(level);
        for (PermissionTreeView node : nodes) {
            System.out.println(indent + "- " + node.name + " (" + node.code + ")");
            if (node.children != null && !node.children.isEmpty()) {
                printTree(node.children, level + 1);
            }
        }
    }
}